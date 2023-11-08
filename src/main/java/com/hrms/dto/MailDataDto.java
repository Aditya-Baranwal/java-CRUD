package com.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailDataDto {
    String to;
    String from;
    String subject;
    String text;
}
