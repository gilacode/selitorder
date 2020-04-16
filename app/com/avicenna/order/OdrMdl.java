package com.avicenna.order;

import com.avicenna.campaign.CampMdl;
import com.avicenna.customer.CustMdl;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "order")
public class OdrMdl extends Model {

    public static class Prov implements Provider<Finder<Long, OdrMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, OdrMdl> get() {
            return new Finder<>(OdrMdl.class);
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

    @NotNull
    private String orderNo;

    @ManyToOne(optional = false)
    private CustMdl customer;

    @ManyToOne(optional = false)
    private CampMdl campaign;

    @NotNull
    private String recName;
    @NotNull
    private String recMobile;
    @NotNull
    private String recAddr;
    @NotNull
    private String recPcode;

    private String odrMsg;

    @NotNull
    private OdrStatus status;

    public enum OdrStatus {

        PENDING("Baru"),
        CONFIRMED("Confirmed"),
        PAID("Telah Dibayar"),
        CLOSED("Selesai"),
        CANCELLED("Batal")
        ;

        private final String message;

        OdrStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
