package com.mushroom.expense.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "capital_comments")
public class CapitalComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capital_transaction_id", nullable = false)
    private CapitalTransaction capitalTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public CapitalComment() {
        this.timestamp = LocalDateTime.now();
    }

    public CapitalComment(CapitalTransaction capitalTransaction, User user, String message) {
        this.capitalTransaction = capitalTransaction;
        this.user = user;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CapitalTransaction getCapitalTransaction() {
        return capitalTransaction;
    }

    public void setCapitalTransaction(CapitalTransaction capitalTransaction) {
        this.capitalTransaction = capitalTransaction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
