package vo;

import models.admin.AdminUser;

public class UserVO extends AbstractServiceVO<UserVO> {

    private String company;

    private String name;

    private String credential;


    private String mobile;

    private String email;

    private String salaryDay;

    private String bankCardNo;

    private String bankCode;

    private String bankName;

    private String bankCity;

    private String bankDetail;

    /**
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String joinDate;

    /**
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String lastLoginDate;

    private Integer remainLoginTimes;

    public UserVO() {
    }

    public static UserVO create(AdminUser user) {
        UserVO vo = new UserVO();

        return vo;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalaryDay() {
        return salaryDay;
    }

    public void setSalaryDay(String salaryDay) {
        this.salaryDay = salaryDay;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }


    public Integer getRemainLoginTimes() {
        return remainLoginTimes;
    }

    public void setRemainLoginTimes(Integer remainLoginTimes) {
        this.remainLoginTimes = remainLoginTimes;
    }

    public String getBankCity() {
        return bankCity;
    }

    public void setBankCity(String bankCity) {
        this.bankCity = bankCity;
    }

    public String getBankDetail() {
        return bankDetail;
    }

    public void setBankDetail(String bankDetail) {
        this.bankDetail = bankDetail;
    }
}
