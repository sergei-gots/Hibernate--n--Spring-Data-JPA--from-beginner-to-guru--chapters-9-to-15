package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Address;
import guru.springframework.jdbc.domain.Customer;
import guru.springframework.jdbc.domain.OrderApproval;
import guru.springframework.jdbc.domain.OrderHeader;
import guru.springframework.jdbc.domain.OrderLine;
import guru.springframework.jdbc.domain.Product;
import guru.springframework.jdbc.enumeration.OrderStatus;
import guru.springframework.jdbc.enumeration.ProductStatus;
import guru.springframework.jdbc.repository.CustomerRepository;
import guru.springframework.jdbc.repository.OrderApprovalRepository;
import guru.springframework.jdbc.repository.OrderHeaderRepository;
import guru.springframework.jdbc.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by sergei on 27/02/2025
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan("guru.springframework.jdbc.dao")
public class OrderHeaderTest {

    @Autowired
    OrderHeaderRepository orderHeaderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderApprovalRepository orderApprovalRepository;

    OrderHeaderDao orderHeaderDao;

    Product product;

    @BeforeEach
    public void setUp() {

        orderHeaderDao = new OrderHeaderDaoImpl(orderHeaderRepository);

        product = new Product();
        product.setProductStatus(ProductStatus.NEW);
        product.setDescription("Test Product");

        product = productRepository.save(product);
    }

    @Test
    public void testEquals() {

        Customer customer = createTestCustomer();
        customerRepository.save(customer);

        OrderHeader orderHeader1 = new OrderHeader();
        customer.addOrderHeader(orderHeader1);

        OrderHeader orderHeader2 = new OrderHeader();
        customer.addOrderHeader(orderHeader2);

        assertThat(orderHeader1).isEqualTo(orderHeader2);
    }

    @Test
    public void testEquals_whenCustomerDiffer() {

        OrderHeader orderHeader1 = new OrderHeader();
        Customer customer1 = new Customer();
        customer1.setCustomerName(RandomString.make(7));
        customerRepository.save(customer1);
        customer1.addOrderHeader(orderHeader1);

        OrderHeader orderHeader2 = new OrderHeader();
        Customer customer2 = new Customer();
        customer2.setCustomerName(RandomString.make(7));
        customerRepository.save(customer2);
        customer2.addOrderHeader(orderHeader2);

        assertEquals(orderHeader1, orderHeader2);
    }

    @Test
    public void testSave() {

        Address address = createTestAddress();

        OrderHeader orderHeader = new OrderHeader();
        Customer customer = createTestCustomer();
        customerRepository.save(customer);
        customer.addOrderHeader(orderHeader);

        orderHeader.setShippingAddress(address);
        orderHeader.setBillingAddress(address);

        OrderApproval orderApproval = new OrderApproval();
        orderApproval.setApprovedBy("me");
        OrderApproval savedOrderApproval = orderApprovalRepository.save(orderApproval);
        orderHeader.setOrderApproval(savedOrderApproval);

        OrderHeader saved = orderHeaderDao.save(orderHeader);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(saved.getCustomer(), orderHeader.getCustomer());
        assertEquals(saved.getBillingAddress(), orderHeader.getBillingAddress());
        assertEquals(saved.getShippingAddress(), orderHeader.getShippingAddress());

        assertEquals(saved.getOrderStatus(), orderHeader.getOrderStatus());
        assertEquals(saved.getOrderApproval(), orderHeader.getOrderApproval());

        assertNotNull(saved.getCreatedDate());
        assertNotNull(saved.getLastModifiedDate());

        long timeDiffMillis = saved.getLastModifiedDate().getTime() - saved.getCreatedDate().getTime();
        assertThat(timeDiffMillis).isLessThan(10);
    }

    @Test
    public void testSaveSave() {

        Address address = createTestAddress();

        OrderHeader orderHeader = new OrderHeader();
        Customer customer = createTestCustomer();
        customerRepository.save(customer);
        customer.addOrderHeader(orderHeader);

        orderHeader.setShippingAddress(address);
        orderHeader.setBillingAddress(address);

        OrderHeader saved = orderHeaderDao.save(orderHeader);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(saved.getCustomer(), orderHeader.getCustomer());
        assertEquals(saved.getBillingAddress(), orderHeader.getBillingAddress());
        assertEquals(saved.getShippingAddress(), orderHeader.getShippingAddress());

        assertNotNull(saved.getCreatedDate());
        assertNotNull(saved.getLastModifiedDate());

        long timeDiffMillis = saved.getLastModifiedDate().getTime() - saved.getCreatedDate().getTime();
        assertThat(timeDiffMillis).isLessThan(10);
    }

    @Test
    public void testSaveWithOrderHeaderAndOrderLine() {

        OrderHeader orderHeader = new OrderHeader();

        Customer customer = createTestCustomer();
        customerRepository.save(customer);
        customer.addOrderHeader(orderHeader);

        OrderLine orderLine = new OrderLine();
        orderLine.setOrderHeader(orderHeader);
        orderLine.setProduct(product);
        orderLine.setQuantityOrdered(1);

        orderHeader.addOrderLine(orderLine);

        OrderHeader savedOrder = orderHeaderDao.save(orderHeader);

        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getOrderLines());
        assertEquals(1, savedOrder.getOrderLines().size());

        OrderLine orderLine1 = savedOrder.getOrderLines().stream().findFirst().orElse(null);

        assertNotNull(orderLine1);
        assertNotNull(orderLine1.getId());
        assertNotNull(orderLine1.getProduct());
        assertEquals(product, orderLine1.getProduct());
        assertEquals(1, orderLine1.getQuantityOrdered());

        assertNotNull(orderLine1.getOrderHeader());
        assertNotNull(orderLine1.getOrderHeader().getId());
        assertNotNull(orderLine1.getOrderHeader().getOrderLines());
        assertEquals(product, orderLine1.getProduct());

    }

    @Test
    public void testGetById() {


        Customer customer = createTestCustomer();
        customerRepository.save(customer);

        OrderHeader orderHeader = new OrderHeader();
        customer.addOrderHeader(orderHeader);
        OrderHeader saved = orderHeaderRepository.save(orderHeader);

        OrderHeader fetched = orderHeaderDao.getById(saved.getId());

        assertThat(fetched).isNotNull();
        assertThat(fetched).isEqualTo(saved);
    }

    @Test
    public void testGetById_whenNotExists_thenThrows() {

        assertThrows(EntityNotFoundException.class, () -> orderHeaderDao.getById(Long.MAX_VALUE));
    }

    @Test
    public void TestFindAll() {

        int pageSize = 10;

        Pageable pageable = PageRequest.of(1, pageSize);

        Page<OrderHeader> orderHeadersPage = orderHeaderDao.findAll(pageable);

        assertThat(orderHeadersPage).isNotNull();

        List<OrderHeader> orderHeaders = orderHeadersPage.getContent();

        assertThat(orderHeaders.size()).isGreaterThanOrEqualTo(0);
        assertThat(orderHeaders.size()).isLessThanOrEqualTo(pageSize);
    }

    @Test
    public void testUpdate() {

        OrderHeader orderHeader = new OrderHeader();

        Customer customer = createTestCustomer();
        customerRepository.save(customer);
        customer.addOrderHeader(orderHeader);

        Address address = createTestAddress();
        orderHeader.setBillingAddress(address);

        OrderHeader persisted = orderHeaderDao.save(orderHeader);

        orderHeader.setOrderStatus(OrderStatus.DELIVERED);

        OrderHeader updated = orderHeaderDao.update(persisted);

        assertThat(updated).isNotNull();
        assertThat(updated).isEqualTo(persisted);
        assertThat(updated.getOrderStatus()).isEqualTo(persisted.getOrderStatus());

        assertNotNull(updated.getCreatedDate());
        assertNotNull(updated.getLastModifiedDate());
        assertThat(updated.getCreatedDate()).isNotEqualTo(updated.getLastModifiedDate());

    }

    private static Address createTestAddress() {
        Address address = new Address();
        address.setAddress("37 West Avenue");
        address.setCity("South Park");
        address.setState("CA");
        address.setZipCode("322233");
        return address;
    }

    @Test
    public void testDeleteById() {

        OrderHeader orderHeader = new OrderHeader();
        Customer customer= createTestCustomer();
        customerRepository.save(customer);
        customer.addOrderHeader(orderHeader);

        OrderHeader saved = orderHeaderDao.save(orderHeader);

        orderHeaderDao.deleteById(saved.getId());

        assertThrows(EntityNotFoundException.class, () -> orderHeaderDao.getById(saved.getId()));
    }

    @Test
    public void testGetByCustomer() {

        Customer customer = createTestCustomer();
        Customer savedCustomer = customerRepository.save(customer);

        OrderHeader orderHeader = new OrderHeader();

        customer.addOrderHeader(orderHeader);

        Customer fetchedCustomer = orderHeader.getCustomer();
        orderHeaderRepository.save(orderHeader);

        OrderHeader fetchedOrderHeader = orderHeaderDao.findOrderHeaderByCustomer(fetchedCustomer);

        assertEquals(customer, savedCustomer);
        assertEquals(customer, fetchedCustomer);

        assertNotNull(fetchedOrderHeader);
        assertEquals(orderHeader.getId(), fetchedOrderHeader.getId());
        assertEquals(customer, fetchedOrderHeader.getCustomer());

        assertNotNull(fetchedOrderHeader.getCustomer().getOrderHeaders());
        assertEquals(1, fetchedOrderHeader.getCustomer().getOrderHeaders().size());

        assertTrue(fetchedCustomer.getOrderHeaders().stream()
                .anyMatch(o -> o.equals(orderHeader)
                )
        );

        assertFalse(fetchedCustomer.getOrderHeaders().contains(orderHeader));

    }
    @Test
    public void testGetByCustomer_assertThat_persistedSet_contains_mayNotWorkProperly() {

        Customer customer = createTestCustomer();
        customerRepository.save(customer);

        OrderHeader orderHeader = new OrderHeader();

        customer.addOrderHeader(orderHeader);

        assertTrue(orderHeader.getCustomer().getOrderHeaders().contains(orderHeader));

        orderHeaderRepository.save(orderHeader);
        Customer fetchedCustomer = orderHeader.getCustomer();

        OrderHeader fetchedOrderHeader = orderHeaderDao.findOrderHeaderByCustomer(fetchedCustomer);

        assertEquals(fetchedOrderHeader, orderHeader);

        assertFalse(fetchedCustomer.getOrderHeaders().contains(orderHeader));
    }


    @Test
    public void testGetByCustomer_whenNotExists_thenThrows() {

        Customer customer = new Customer();
        customer.setCustomerName("Customer#that#is#not#in#db" + RandomString.make(10));
        customerRepository.save(customer);

        assertThrows(EntityNotFoundException.class, () -> orderHeaderDao.findOrderHeaderByCustomer(customer));

    }

    private static Customer createTestCustomer() {

        Customer customer = new Customer();

        customer.setCustomerName("Customer#" + RandomString.make(10));

        return customer;
    }

}