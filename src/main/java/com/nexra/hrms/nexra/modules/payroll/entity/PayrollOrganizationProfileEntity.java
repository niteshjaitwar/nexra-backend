package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.modules.payroll.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_organization_profiles")
public class PayrollOrganizationProfileEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60, unique = true)
    private String tenantCode;

    @Column(name = "organization_name", nullable = false, length = 180)
    private String organizationName;

    @Column(name = "legal_entity_name", nullable = false, length = 180)
    private String legalEntityName;

    @Column(name = "address_line1", nullable = false, length = 180)
    private String addressLine1;

    @Column(name = "address_line2", length = 180)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 120)
    private String city;

    @Column(name = "state", nullable = false, length = 120)
    private String state;

    @Column(name = "country", nullable = false, length = 120)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "default_tax_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultTaxPercent;

    @Column(name = "default_pf_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultProvidentFundPercent;

    @Column(name = "payroll_contact_email", length = 180)
    private String payrollContactEmail;

    @Column(name = "payroll_contact_phone", length = 40)
    private String payrollContactPhone;

    @Column(name = "branding_logo_path", length = 255)
    private String brandingLogoPath;

    @Column(name = "branding_company_name", length = 180)
    private String brandingCompanyName;

    @Column(name = "branding_watermark_text", length = 120)
    private String brandingWatermarkText;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(final String organizationName) {
        this.organizationName = organizationName;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(final String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public BigDecimal getDefaultTaxPercent() {
        return defaultTaxPercent;
    }

    public void setDefaultTaxPercent(final BigDecimal defaultTaxPercent) {
        this.defaultTaxPercent = defaultTaxPercent;
    }

    public BigDecimal getDefaultProvidentFundPercent() {
        return defaultProvidentFundPercent;
    }

    public void setDefaultProvidentFundPercent(final BigDecimal defaultProvidentFundPercent) {
        this.defaultProvidentFundPercent = defaultProvidentFundPercent;
    }

    public String getPayrollContactEmail() {
        return payrollContactEmail;
    }

    public void setPayrollContactEmail(final String payrollContactEmail) {
        this.payrollContactEmail = payrollContactEmail;
    }

    public String getPayrollContactPhone() {
        return payrollContactPhone;
    }

    public void setPayrollContactPhone(final String payrollContactPhone) {
        this.payrollContactPhone = payrollContactPhone;
    }

    public String getBrandingLogoPath() {
        return brandingLogoPath;
    }

    public void setBrandingLogoPath(final String brandingLogoPath) {
        this.brandingLogoPath = brandingLogoPath;
    }

    public String getBrandingCompanyName() {
        return brandingCompanyName;
    }

    public void setBrandingCompanyName(final String brandingCompanyName) {
        this.brandingCompanyName = brandingCompanyName;
    }

    public String getBrandingWatermarkText() {
        return brandingWatermarkText;
    }

    public void setBrandingWatermarkText(final String brandingWatermarkText) {
        this.brandingWatermarkText = brandingWatermarkText;
    }
}
