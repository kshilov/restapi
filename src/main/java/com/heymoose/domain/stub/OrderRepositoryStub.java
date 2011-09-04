package com.heymoose.domain.stub;

import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;

import javax.inject.Singleton;

@Singleton
public class OrderRepositoryStub extends RepositoryStub<Order> implements OrderRepository {
}
