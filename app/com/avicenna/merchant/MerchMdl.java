package com.avicenna.merchant;

import com.avicenna.security.SecUserMdl;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "merchant")
public class MerchMdl extends Model {

    public static class Prov implements Provider<Finder<Long, MerchMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, MerchMdl> get() {
            return new Finder<>(MerchMdl.class);
        }
    }

    @Id
    private Long id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @NotNull
    private String createdBy;

    @OneToOne(optional = false)
    private SecUserMdl userRef;

    private boolean premium;

    @NotNull
    private String storeName;

    @NotNull
    private String storeLogoUrl;

    @NotNull
    private String bizMain;
    @NotNull
    private String bizPcode;

    // updated everytime there's new review
    @Column(precision = 5, scale = 1)
    private BigDecimal ratings;
    private int reviewCount;

    @NotNull
    private MerchStatus status;

    public enum MerchStatus {

        NEW("Baru"),
        VERIFIED("Aktif"),
        SUSPEND("Digantung")
        ;

        private final String message;

        MerchStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
