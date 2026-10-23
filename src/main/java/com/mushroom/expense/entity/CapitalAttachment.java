package com.mushroom.expense.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "capital_attachments")
public class CapitalAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capital_transaction_id", nullable = false)
    private CapitalTransaction capitalTransaction;

    public CapitalAttachment() {
    }

    public CapitalAttachment(String fileName, CapitalTransaction capitalTransaction) {
        this.fileName = fileName;
        this.capitalTransaction = capitalTransaction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public CapitalTransaction getCapitalTransaction() {
        return capitalTransaction;
    }

    public void setCapitalTransaction(CapitalTransaction capitalTransaction) {
        this.capitalTransaction = capitalTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CapitalAttachment that = (CapitalAttachment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
