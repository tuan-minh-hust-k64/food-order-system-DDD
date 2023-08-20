package com.food.ordering.system.order.service.dataaccess.customer.entity;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntityId;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "customers")
@Entity
public class  CustomerEntity {
    @Id
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}
