package com.kodlamaio.commonpackage.events.inventory;

import com.kodlamaio.commonpackage.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarDeletedEvent extends BaseEvent
{
    private UUID carId;
}
