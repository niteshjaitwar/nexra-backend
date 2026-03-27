package com.nexra.hrms.nexra.modules.payroll.model;

import java.math.BigDecimal;

public record PayrollLineItem(String name, BigDecimal amount) {
}
