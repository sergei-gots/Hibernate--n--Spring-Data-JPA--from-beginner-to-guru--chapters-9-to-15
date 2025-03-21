package guru.springframework.orderservice.domain;

import guru.springframework.orderservice.enumeration.OrderStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by sergei on 02/03/2025
 */
@Entity
@AttributeOverrides({
        @AttributeOverride(name = "shippingAddress.address", column = @Column(name = "shipping_address")),
        @AttributeOverride(name = "shippingAddress.city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "shippingAddress.state", column = @Column(name = "shipping_state")),
        @AttributeOverride(name = "shippingAddress.zipCode", column = @Column(name = "shipping_zip_code")),
        @AttributeOverride(name = "billingAddress.address", column = @Column(name = "billing_address")),
        @AttributeOverride(name = "billingAddress.city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "billingAddress.state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "billingAddress.zipCode", column = @Column(name = "billing_zip_code")),
})
public class OrderHeader extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @Embedded
    private Address shippingAddress;
    @Embedded
    private Address billingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "orderHeader", cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    Set<OrderLine> orderLines;

    @OneToOne(mappedBy = "orderHeader", cascade = {CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    private OrderApproval orderApproval;

    @Version
    private Integer version;

    public OrderHeader() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;

        if (customer != null) {
            customer.addOrderHeader(this);
        }
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public Set<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public void setOrderApproval(OrderApproval orderApproval) {

        if (orderApproval != null) {
            orderApproval.setOrderHeader(this);
        }
        this.orderApproval = orderApproval;
    }

    @Override
    public String toString() {
        return "OrderHeader{" +
                "id=" + id + '\'' +
                "customer='" + customer + '\'' +
                "order lines=" + orderLines +
                ", shippingAddress=" + shippingAddress + '\'' +
                ", billingAddress=" + billingAddress  + '\'' +
                ", orderStatus=" + orderStatus +
                ", orderApproval=" + orderApproval +
                '}';
    }

    public void addOrderLine(OrderLine orderLine) {

        if (orderLines == null) {
            orderLines = new LinkedHashSet<>();
        }

        orderLine.setOrderHeader(this);
        orderLines.add(orderLine);
    }

}
