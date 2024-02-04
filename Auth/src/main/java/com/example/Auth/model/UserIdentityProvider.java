package com.example.Auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_identity_providers")
public class UserIdentityProvider {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private IdentityProvider identityProvider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
}

