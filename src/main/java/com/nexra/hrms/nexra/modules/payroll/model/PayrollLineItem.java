package com.nexra.hrms.nexra.modules.payroll.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public record PayrollLineItem(@JsonAlias("label") String name, BigDecimal amount) {
}
