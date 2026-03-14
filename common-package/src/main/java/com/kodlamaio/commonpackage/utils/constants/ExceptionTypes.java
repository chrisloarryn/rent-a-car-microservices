package com.kodlamaio.commonpackage.utils.constants;

public class ExceptionTypes
{
    private ExceptionTypes() {
    }

    public static class Exception
    {
        private Exception() {
        }

        public static final String Authorization = "AUTHORIZATION_EXCEPTION";
        public static final String Validation = "VALIDATION_EXCEPTION";
        public static final String Business = "BUSINESS_EXCEPTION";
        public static final String Runtime = "RUNTIME_EXCEPTION";
        public static final String DataIntegrityViolation = "DATA_INTEGRITY_VIOLATION_EXCEPTION";
        public static final String RequestFormat = "REQUEST_FORMAT_EXCEPTION";
        public static final String ConstraintViolation = "CONSTRAINT_VIOLATION_EXCEPTION";
    }
}
