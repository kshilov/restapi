package com.heymoose.domain.affiliate.hiber;

import com.heymoose.domain.affiliate.KeywordPattern;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URLEncodedUtils;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Singleton
public class KeywordPatternDao {

  private final Repo repo;

  private static volatile Map<Pattern, String> cache = new HashMap<Pattern, String>();
  private static volatile DateTime timestamp;
  private static final int CACHE_TTL_SECONDS = 60;

  @Inject
  public KeywordPatternDao(Repo repo) {
    this.repo = repo;
    fetchKeywordPatters();
  }

  @Transactional
  public void fetchKeywordPatters() {
    Map<Pattern, String> newCache = new HashMap<Pattern, String>();
    List<KeywordPattern> keywordPatterns = repo.allByHQL(KeywordPattern.class, "from KeywordPattern");

    if (keywordPatterns.size() != 0) {
      for (KeywordPattern entry : keywordPatterns)
        newCache.put(Pattern.compile("(.*)" + entry.urlPattern() + "(.*)"), entry.keywordsParameter());
    }

    synchronized (KeywordPatternDao.class) {
      cache = newCache;
      timestamp = DateTime.now();
    }
  }

  @Transactional
  public String extractKeywords(String query) {
    if (query == null) return null;

    // renew cache
    if (timestamp.plusSeconds(CACHE_TTL_SECONDS).isBeforeNow()) {
      fetchKeywordPatters();
    }

    for (Pattern p : cache.keySet()) {
      if (p.matcher(query).matches()) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(URI.create(query), "UTF-8");
        for (NameValuePair pair : pairs) {
          if (cache.get(p).equals(pair.fst)) return pair.snd;
        }
      }
    }

    // not found
    return null;
  }
}
