package com.avicenna.merchant;

import com.avicenna.bank.BankMdl;
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
@Table(name = "merchant_bank")
public class MerchBankMdl extends Model {

    public static class Prov implements Provider<Finder<Long, MerchBankMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, MerchBankMdl> get() {
            return new Finder<>(MerchBankMdl.class);
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

    @ManyToOne(optional = false)
    private BankMdl bank;


}
