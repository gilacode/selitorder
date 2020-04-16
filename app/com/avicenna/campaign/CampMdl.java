package com.avicenna.campaign;

import com.avicenna.merchant.MerchMdl;
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
@Table(name = "campaign")
public class CampMdl extends Model {

    public static class Prov implements Provider<Finder<Long, CampMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, CampMdl> get() {
            return new Finder<>(CampMdl.class);
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

    @ManyToOne(optional = false)
    private MerchMdl merchant;

    @NotNull
    private String campName;

    @Lob
    private String campDesc;

    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal minDelCharge; // copy form merchant
    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal maxDelCharge; // copy from merchant

    private int dailyClsTime; // closing time in hour * 60
    private boolean runOnMon;
    private boolean runOnTue;
    private boolean runOnWed;
    private boolean runOnThu;
    private boolean runOnFri;
    private boolean runOnSat;
    private boolean runOnSun;

    private Date expiryDate;

    @NotNull
    private String delAddr; // copy from merchant
    @NotNull
    private String delPCode; // copy from merchant

    private boolean fixStock;

    @NotNull
    private CampStatus status;

    private boolean deleted;

    public enum CampStatus {

        DRAFT("Deraf"),
        ACTIVE("Aktif"),
        PAUSED("Paused")
        ;

        private final String message;

        CampStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
