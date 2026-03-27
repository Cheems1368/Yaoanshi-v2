package com.medicine.domain.drug;

import com.medicine.domain.common.BaseEntity;

public class DrugCatalog extends BaseEntity {

    private String nameCn;
    private String nameEn;
    private String pinyinInitials;
    private String category;
    private String status;

    public String getNameCn() { return nameCn; }
    public void setNameCn(String nameCn) { this.nameCn = nameCn; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getPinyinInitials() { return pinyinInitials; }
    public void setPinyinInitials(String pinyinInitials) { this.pinyinInitials = pinyinInitials; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
