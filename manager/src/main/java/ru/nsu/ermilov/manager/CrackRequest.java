package ru.nsu.ermilov.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CrackRequest {

    private String Hash;

    private Integer MaxLength;

}
