package com.algolia.search.integration.index;

import static org.assertj.core.api.Assertions.*;

import com.algolia.search.clients.SearchIndex;
import com.algolia.search.integration.AlgoliaBaseIntegrationTest;
import com.algolia.search.models.common.BatchIndexingResponse;
import com.algolia.search.models.search.*;
import com.algolia.search.models.settings.IndexSettings;
import com.algolia.search.models.settings.SetSettingsResponse;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class SearchTest extends AlgoliaBaseIntegrationTest {

  private SearchIndex<Employee> index;
  private List<Employee> employees;

  void init() {
    index = searchClient.initIndex(getTestIndexName("search"), Employee.class);
    employees =
        Arrays.asList(
            new Employee("Algolia", "Julien Lemoine"),
            new Employee("Algolia", "Julien Lemoine"),
            new Employee("Amazon", "Jeff Bezos"),
            new Employee("Apple", "Steve Jobs"),
            new Employee("Apple", "Steve Wozniak"),
            new Employee("Arista Networks", "Jayshree Ullal"),
            new Employee("Google", "Lary Page"),
            new Employee("Google", "Rob Pike"),
            new Employee("Google", "Sergueï Brin"),
            new Employee("Microsoft", "Bill Gates"),
            new Employee("SpaceX", "Elon Musk"),
            new Employee("Tesla", "Elon Musk"),
            new Employee("Yahoo", "Marissa Mayer"));
  }

  @Test
  void testSearch() throws ExecutionException, InterruptedException {
    init();

    CompletableFuture<BatchIndexingResponse> saveObjectsFuture =
        index.saveObjectsAsync(employees, true);
    IndexSettings settings =
        new IndexSettings()
            .setAttributesForFaceting(Collections.singletonList("searchable(company)"));

    saveObjectsFuture.get().waitTask();

    CompletableFuture<SetSettingsResponse> setSettingsFuture = index.setSettingsAsync(settings);
    setSettingsFuture.get().waitTask();

    CompletableFuture<SearchResult<Employee>> searchAlgoliaFuture =
        index.searchAsync(new Query("algolia"));

    CompletableFuture<SearchResult<Employee>> searchElonFuture =
        index.searchAsync(new Query("elon").setClickAnalytics(true));

    CompletableFuture<SearchResult<Employee>> searchElonFuture1 =
        index.searchAsync(
            new Query("elon")
                .setFacets(Collections.singletonList("*"))
                .setFacetFilters(
                    Collections.singletonList(Collections.singletonList("company:tesla"))));

    CompletableFuture<SearchResult<Employee>> searchElonFuture2 =
        index.searchAsync(
            new Query("elon")
                .setFacets(Collections.singletonList("*"))
                .setFilters("(company:tesla OR company:spacex)"));

    CompletableFuture<SearchForFacetResponse> searchFacetFuture =
        index.searchForFacetValuesAsync(
            new SearchForFacetRequest().setFacetName("company").setFacetQuery("a"));

    CompletableFuture.allOf(
        searchAlgoliaFuture,
        searchElonFuture,
        searchElonFuture1,
        searchElonFuture2,
        searchElonFuture2,
        searchFacetFuture);

    assertThat(searchAlgoliaFuture.get().getHits()).hasSize(2);
    assertThat(searchElonFuture.get().getQueryID()).isNotNull();
    assertThat(searchElonFuture1.get().getHits()).hasSize(1);
    assertThat(searchElonFuture2.get().getHits()).hasSize(2);
    assertThat(searchFacetFuture.get().getFacetHits())
        .extracting(FacetHit::getValue)
        .contains("Algolia");
    assertThat(searchFacetFuture.get().getFacetHits())
        .extracting(FacetHit::getValue)
        .contains("Amazon");
    assertThat(searchFacetFuture.get().getFacetHits())
        .extracting(FacetHit::getValue)
        .contains("Apple");
    assertThat(searchFacetFuture.get().getFacetHits())
        .extracting(FacetHit::getValue)
        .contains("Arista Networks");
  }
}

class Employee implements Serializable {

  public Employee() {}

  Employee(String company, String name) {
    this.company = company;
    this.name = name;
  }

  public String getCompany() {
    return company;
  }

  public Employee setCompany(String company) {
    this.company = company;
    return this;
  }

  public String getName() {
    return name;
  }

  public Employee setName(String name) {
    this.name = name;
    return this;
  }

  public String getQueryID() {
    return queryID;
  }

  public Employee setQueryID(String queryID) {
    this.queryID = queryID;
    return this;
  }

  private String company;
  private String name;
  private String queryID;
}
