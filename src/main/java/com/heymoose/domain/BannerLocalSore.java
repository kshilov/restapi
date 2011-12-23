package com.heymoose.domain;

import com.heymoose.cache.ThreadLocalMapStore;
import javax.inject.Singleton;

// offer.id -> banner
@Singleton
public class BannerLocalSore extends ThreadLocalMapStore<Long, Banner> {

}