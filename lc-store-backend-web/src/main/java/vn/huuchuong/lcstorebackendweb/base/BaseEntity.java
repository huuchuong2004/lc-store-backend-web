package vn.huuchuong.lcstorebackendweb.base;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass // không phải là bảng DB riêng, nhưng các entity con sẽ kế thừa cột này
@EntityListeners(AuditingEntityListener.class) // bật auditing // khi update hay chinh sua se tu dong cap nhat cac createat hay updateat
public abstract class BaseEntity {


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
