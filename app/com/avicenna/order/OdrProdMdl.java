package com.avicenna.order;

import com.avicenna.campaign.CampMdl;
import com.avicenna.customer.CustMdl;
import com.avicenna.product.ProdMdl;
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
@Table(name = "order_prod")
public class OdrProdMdl extends Model {

    public static class Prov implements Provider<Finder<Long, OdrProdMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, OdrProdMdl> get() {
            return new Finder<>(OdrProdMdl.class);
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
    private OdrMdl order;

    @ManyToOne(optional = false)
    private ProdMdl product;

    private int odrQty;
    private int confirmQty;

    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal priceToPay; // copy from product


}
