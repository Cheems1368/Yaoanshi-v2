package com.medicine.api.dto.drug;

public class DrugCatalogDTO {

    private Long id;
    private String nameCn;
    private String nameEn;
    private String pinyinInitials;
    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNameCn() { return nameCn; }
    public void setNameCn(String nameCn) { this.nameCn = nameCn; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getPinyinInitials() { return pinyinInitials; }
    public void setPinyinInitials(String pinyinInitials) { this.pinyinInitials = pinyinInitials; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
