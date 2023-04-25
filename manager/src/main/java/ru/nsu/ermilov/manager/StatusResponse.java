package ru.nsu.ermilov.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class StatusResponse {

    private Status status;

    private List<String> data;
}
