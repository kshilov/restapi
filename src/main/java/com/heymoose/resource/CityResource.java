package com.heymoose.resource;

import com.heymoose.domain.City;
import com.heymoose.domain.CityRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.resource.xml.Mappers.toXmlCities;
import com.heymoose.resource.xml.XmlCities;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("cities")
public class CityResource {

  private final CityRepository cities;

  @Inject
  public CityResource(CityRepository cities) {
    this.cities = cities;
  }

  @POST
  @Transactional
  public String create(@FormParam("name") String name) {
    City city = new City(name);
    cities.put(city);
    return Long.toString(city.id());
  }

  @Path("{id}")
  @PUT
  @Transactional
  public void update(@PathParam("id") long id, @FormParam("name") String name) {
    City city = cities.byId(id);
    if (city == null)
      throw notFound();
    city.changeName(name);
  }

  @Path("{id}")
  @DELETE
  @Transactional
  public void delete(@PathParam("id") long id) {
    City city = cities.byId(id);
    if (city == null)
      throw notFound();
    cities.remove(city);
  }

  @GET
  @Transactional
  public XmlCities all() {
    return toXmlCities(cities.all());
  }
}
