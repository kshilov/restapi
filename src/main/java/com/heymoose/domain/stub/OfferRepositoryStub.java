package com.heymoose.domain.stub;

import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;

import javax.inject.Singleton;

@Singleton
public class OfferRepositoryStub extends RepositoryStub<Offer> implements OfferRepository {
}
