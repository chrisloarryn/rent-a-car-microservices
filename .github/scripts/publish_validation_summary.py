#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path


def append_summary(summary_file: str | None, content: str) -> None:
    if summary_file:
        with Path(summary_file).open("a", encoding="utf-8") as handle:
            handle.write(content.rstrip() + "\n")


def write_output(output_file: str | None, key: str, value: str) -> None:
    if output_file:
        with Path(output_file).open("a", encoding="utf-8") as handle:
            handle.write(f"{key}={value}\n")


def module_from_path(path: Path) -> str:
    parts = path.parts
    if "target" in parts:
        target_index = parts.index("target")
        if target_index > 0:
            return parts[target_index - 1]
    return path.parent.name


def filtered_files(root: Path, pattern: str, module: str | None) -> list[Path]:
    files = sorted(root.rglob(pattern))
    if module is None:
        return files
    return [path for path in files if module_from_path(path) == module]


def parse_unit(args: argparse.Namespace) -> int:
    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0, "duration": 0.0}
    modules: dict[str, dict[str, float]] = {}

    for report in filtered_files(Path(args.repo_root), "TEST-*.xml", args.module):
        root = ET.parse(report).getroot()
        suite_name = root.attrib.get("name", report.stem.removeprefix("TEST-"))
        if suite_name == "karate.ApiContractsKarateTest":
            continue

        module = module_from_path(report)
        module_totals = modules.setdefault(module, {"tests": 0, "failures": 0, "errors": 0, "skipped": 0, "duration": 0.0})
        tests = int(root.attrib.get("tests", "0"))
        failures = int(root.attrib.get("failures", "0"))
        errors = int(root.attrib.get("errors", "0"))
        skipped = int(root.attrib.get("skipped", "0"))
        duration = float(root.attrib.get("time", "0"))

        totals["tests"] += tests
        totals["failures"] += failures
        totals["errors"] += errors
        totals["skipped"] += skipped
        totals["duration"] += duration

        module_totals["tests"] += tests
        module_totals["failures"] += failures
        module_totals["errors"] += errors
        module_totals["skipped"] += skipped
        module_totals["duration"] += duration

    write_output(args.output_file, "tests_run", str(totals["tests"]))
    write_output(args.output_file, "failures", str(totals["failures"]))
    write_output(args.output_file, "errors", str(totals["errors"]))
    write_output(args.output_file, "skipped", str(totals["skipped"]))

    module_lines = "\n".join(
        f"| `{module}` | {int(values['tests'])} | {int(values['failures'])} | {int(values['errors'])} | {int(values['skipped'])} | {values['duration']:.2f}s |"
        for module, values in sorted(modules.items())
    ) or "| No module reports found | 0 | 0 | 0 | 0 | 0.00s |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Unit and Integration Tests",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Tests run | {totals['tests']} |",
                f"| Failures | {totals['failures']} |",
                f"| Errors | {totals['errors']} |",
                f"| Skipped | {totals['skipped']} |",
                f"| Duration | {totals['duration']:.2f}s |",
                "",
                "| Module | Tests | Failures | Errors | Skipped | Duration |",
                "| --- | ---: | ---: | ---: | ---: | ---: |",
                module_lines,
                "",
            ]
        ),
    )
    return 0


def parse_coverage(args: argparse.Namespace) -> int:
    modules: dict[str, dict[str, float]] = {}
    total_covered = 0
    total_missed = 0

    for report in filtered_files(Path(args.repo_root), "jacoco.xml", args.module):
        root = ET.parse(report).getroot()
        counter = next((counter for counter in root.findall("counter") if counter.attrib.get("type") == "LINE"), None)
        if counter is None:
            continue

        covered = int(counter.attrib.get("covered", "0"))
        missed = int(counter.attrib.get("missed", "0"))
        ratio = (covered / (covered + missed) * 100.0) if (covered + missed) else 0.0
        module = module_from_path(report)

        modules[module] = {"covered": covered, "missed": missed, "ratio": ratio}
        total_covered += covered
        total_missed += missed

    total_ratio = (total_covered / (total_covered + total_missed) * 100.0) if (total_covered + total_missed) else 0.0
    failing_modules = [
        module for module, values in modules.items()
        if values["ratio"] < args.minimum_ratio * 100.0
    ]

    write_output(args.output_file, "line_coverage_pct", f"{total_ratio:.2f}")
    write_output(args.output_file, "covered_lines", str(total_covered))
    write_output(args.output_file, "missed_lines", str(total_missed))
    write_output(args.output_file, "coverage_threshold_pct", f"{args.minimum_ratio * 100.0:.2f}")

    module_lines = "\n".join(
        f"| `{module}` | {values['ratio']:.2f}% | {values['covered']} | {values['missed']} | {'PASS' if module not in failing_modules else 'FAIL'} |"
        for module, values in sorted(modules.items())
    ) or "| No coverage reports found | 0.00% | 0 | 0 | n/a |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Coverage Quality Gate",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Aggregate line coverage | {total_ratio:.2f}% |",
                f"| Coverage threshold | {args.minimum_ratio * 100.0:.2f}% |",
                f"| Covered lines | {total_covered} |",
                f"| Missed lines | {total_missed} |",
                "",
                "| Module | Line coverage | Covered | Missed | Gate |",
                "| --- | ---: | ---: | ---: | --- |",
                module_lines,
                "",
            ]
        ),
    )
    return 0


def parse_karate(args: argparse.Namespace) -> int:
    modules: dict[str, dict[str, float]] = {}

    for summary in filtered_files(Path(args.repo_root), "karate-summary-json.txt", args.module):
        data = json.loads(summary.read_text(encoding="utf-8"))
        module = module_from_path(summary)
        features_failed = data.get("featuresFailed", 0)
        features_total = data.get("featuresPassed", 0) + features_failed + data.get("featuresSkipped", 0)
        scenarios_failed = data.get("scenariosfailed", data.get("scenariosFailed", 0))
        scenarios_total = data.get("scenariosPassed", 0) + scenarios_failed
        elapsed_seconds = float(data.get("elapsedTime", 0)) / 1000.0

        modules[module] = {
            "features_total": features_total,
            "features_failed": features_failed,
            "scenarios_total": scenarios_total,
            "scenarios_failed": scenarios_failed,
            "elapsed_seconds": elapsed_seconds,
        }

    total_failed = sum(int(values["scenarios_failed"]) for values in modules.values())
    write_output(args.output_file, "scenarios_failed", str(total_failed))

    module_lines = "\n".join(
        f"| `{module}` | {int(values['features_total'])} | {int(values['features_failed'])} | {int(values['scenarios_total'])} | {int(values['scenarios_failed'])} | {values['elapsed_seconds']:.2f}s |"
        for module, values in sorted(modules.items())
    ) or "| No Karate reports found | 0 | 0 | 0 | 0 | 0.00s |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Karate Contract Tests",
                "",
                "| Module | Features | Failed features | Scenarios | Failed scenarios | Duration |",
                "| --- | ---: | ---: | ---: | ---: | ---: |",
                module_lines,
                "",
            ]
        ),
    )
    return 0


def extract_gatling_metrics(console_log: Path) -> dict[str, str]:
    text = console_log.read_text(encoding="utf-8", errors="replace")
    metrics = {
        "requests_total": "0",
        "requests_ok": "0",
        "requests_ko": "0",
        "p95_ms": "n/a",
        "p99_ms": "n/a",
        "throughput_rps": "n/a",
        "assertions_failed": "0",
    }

    for raw_line in text.splitlines():
        stripped = raw_line.strip()
        if stripped.startswith("> request count"):
            columns = [column.strip().replace(",", "") for column in raw_line.split("|")[1:]]
            if len(columns) >= 3:
                metrics["requests_total"] = columns[0]
                metrics["requests_ok"] = columns[1]
                metrics["requests_ko"] = columns[2].replace("-", "0")
        elif stripped.startswith("> response time 95th percentile (ms)"):
            columns = [column.strip().replace(",", "") for column in raw_line.split("|")[1:]]
            if columns:
                metrics["p95_ms"] = columns[0]
        elif stripped.startswith("> response time 99th percentile (ms)"):
            columns = [column.strip().replace(",", "") for column in raw_line.split("|")[1:]]
            if columns:
                metrics["p99_ms"] = columns[0]
        elif stripped.startswith("> mean throughput (rps)"):
            columns = [column.strip().replace(",", "") for column in raw_line.split("|")[1:]]
            if columns:
                metrics["throughput_rps"] = columns[0]

    assertion_results = [
        line for line in text.splitlines()
        if re.match(r"^.* : (true|false) \(actual : .*", line.strip())
    ]
    metrics["assertions_failed"] = str(sum(1 for line in assertion_results if " : false " in line))
    return metrics


def parse_gatling(args: argparse.Namespace) -> int:
    modules: dict[str, dict[str, str]] = {}

    for console_log in filtered_files(Path(args.repo_root), "gatling-console.log", args.module):
        modules[module_from_path(console_log)] = extract_gatling_metrics(console_log)

    total_failed_assertions = sum(int(values["assertions_failed"]) for values in modules.values())
    write_output(args.output_file, "assertions_failed", str(total_failed_assertions))

    module_lines = "\n".join(
        f"| `{module}` | {values['requests_total']} | {values['requests_ko']} | {values['p95_ms']} | {values['p99_ms']} | {values['throughput_rps']} | {values['assertions_failed']} |"
        for module, values in sorted(modules.items())
    ) or "| No Gatling logs found | 0 | 0 | n/a | n/a | n/a | 0 |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Gatling Performance Tests",
                "",
                "| Module | Requests | KO | p95 (ms) | p99 (ms) | Throughput (rps) | Failed assertions |",
                "| --- | ---: | ---: | ---: | ---: | ---: | ---: |",
                module_lines,
                "",
            ]
        ),
    )
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)

    for command in ("unit", "coverage", "karate", "gatling"):
        subparser = subparsers.add_parser(command)
        subparser.add_argument("--repo-root", required=True)
        subparser.add_argument("--summary-file")
        subparser.add_argument("--output-file")
        subparser.add_argument("--module")
        if command == "coverage":
            subparser.add_argument("--minimum-ratio", type=float, default=0.90)

    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    if args.command == "unit":
        return parse_unit(args)
    if args.command == "coverage":
        return parse_coverage(args)
    if args.command == "karate":
        return parse_karate(args)
    return parse_gatling(args)


if __name__ == "__main__":
    raise SystemExit(main())
