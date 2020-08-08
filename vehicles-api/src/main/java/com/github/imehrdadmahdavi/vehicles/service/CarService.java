package com.github.imehrdadmahdavi.vehicles.service;

import com.github.imehrdadmahdavi.vehicles.client.maps.MapsClient;
import com.github.imehrdadmahdavi.vehicles.client.prices.PriceClient;
import com.github.imehrdadmahdavi.vehicles.domain.car.Car;
import com.github.imehrdadmahdavi.vehicles.domain.car.CarRepository;
import com.github.imehrdadmahdavi.vehicles.domain.Location;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    public CarService(CarRepository repository, MapsClient mapsClient, PriceClient priceClient) {
        this.repository = repository;
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
    }
    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {

        Car car = repository.findById(id).orElseThrow(CarNotFoundException::new);

        /**
         * The car class file uses @transient, meaning you will need to call
         * the pricing service each time to get the price.
         */
        car.setPrice(priceClient.getPrice(id));


        /**
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        Location location = car.getLocation();
        Location updatedLocation = mapsClient.getAddress(location);
        car.setLocation(updatedLocation);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {

        Car car = this.findById(id);
        repository.delete(car);
    }
}
