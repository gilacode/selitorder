package com.avicenna.product;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.Index;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "product")
public class ProdMdl extends Model {

    public static class Prov implements Provider<Finder<Long, ProdMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject Prov(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, ProdMdl> get() {
            return new Finder<>(ProdMdl.class);
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
    private String prodName;

    @Lob
    private String prodDesc;

    @NotNull
    private String prodPhotoUrl;

    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal price; // latest price. history is save in product history

    @NotNull
    private ProdStatus status;

    private boolean deleted;

    public enum ProdStatus {

        DRAFT("Deraf"),
        ACTIVE("Aktif"),
        PAUSED("Paused")
        ;

        private final String message;

        ProdStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
