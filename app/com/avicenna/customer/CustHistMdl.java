package com.avicenna.customer;

import com.avicenna.bank.BankMdl;
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
import java.util.Date;

@Entity
@Table(name = "customer_history")
public class CustHistMdl extends Model {

    public static class Prov implements Provider<Finder<Long, CustHistMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, CustHistMdl> get() {
            return new Finder<>(CustHistMdl.class);
        }
    }

    @Id
    private Long id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @NotNull
    private String updatedBy;


    private String username;

    @NotNull
    private String addrMain;
    @NotNull
    private String addrMainPcode;

    private boolean sameAsMainAddr;
    private String addrBill;
    private String addrBillPcode;

    private boolean affiliate;
    @ManyToOne
    private BankMdl custBank;
    private String bankAccNo;

    @NotNull
    private CustStatus status;

    public enum CustStatus {

        NEW("Baru"),
        VERIFIED("Aktif"),
        SUSPEND("Digantung")
        ;

        private final String message;

        CustStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
