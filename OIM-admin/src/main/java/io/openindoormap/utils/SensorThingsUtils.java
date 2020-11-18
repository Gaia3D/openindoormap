package io.openindoormap.utils;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geojson.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.threeten.extra.Interval;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class SensorThingsUtils {

    @Autowired
    private SensorThingsService service;

    private Boolean dryRun = true;

    /**
     * STA 서비스 초기화
     * @param url
     */
    public void init(String url) {
        try {
            URL serviceEndpoint = new URL(url);
            service = new SensorThingsService(serviceEndpoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 사용중인 서비스 가져오기
     * @return SensorThingsService
     */
    public SensorThingsService getService() {
        return service;
    }

     /**
     * STA Entity 생성
     * @param <T>
     * @param entity
     */
    public <T extends Entity<T>> void create(T entity) {
        if (dryRun) {
            log.info("Dry Run: Not creating entity..." + entity.toString());
        } else {
            try {
                service.create(entity);
            } catch (ServiceFailureException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * STA Entity 수정
     * @param <T>
     * @param entity
     */
    public <T extends Entity<T>> void update(T entity) {
        if (dryRun) {
            log.info("Dry Run: Not updating entity..." + entity);
        } else {
            try {
                service.update(entity);
            } catch (ServiceFailureException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 필터를 통해 특정 조건으로 STA Entity 를 검색
     * @param <T> Entity
     * @param query A query for reading operations.
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return
     */
    public static <T extends Entity<T>> Query<T> createFilter(final Query<T> query, final String filter,
            final String name) {
        String filterString = filter;

        if (StringUtils.isBlank(filterString)) {
            filterString = "name eq '" + name + "'";
        }
        return query.filter(filterString);
    }

    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(Thing) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return Thing
     */
    public Thing hasThing(String filter, String name) {
        Thing thing = null;
        Query<Thing> query = service.things().query();

        try {
            EntityList<Thing> list;
            list = createFilter(query, filter, name).expand("Locations($select=id)").list();
            if (list.size() == 1) {
                thing = list.iterator().next();
            } else {
                log.debug("More than one entity(Thing) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return thing;
    }

    public Thing hasThingWithAllEntity(String filter, String name) {
        Thing thing = null;
        Query<Thing> query = service.things().query();

        try {
            EntityList<Thing> list;
            list = createFilter(query, filter, name)
                    .select("id")
                    .expand("Locations($select=id),Datastreams($expand=Sensor($select=id),ObservedProperty($select=id))").list();
            if (list.size() == 1) {
                thing = list.iterator().next();
            } else {
                log.debug("More than one entity(Thing) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return thing;
    }

    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(Thing) 를 검색
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return
     */
    public EntityList<Thing> hasThingWithObservation(String filter, String name) {
        EntityList<Thing> thingList = null;
        Query<Thing> query = service.things().query();

        try {
            thingList = createFilter(query, filter, name).expand("Datastreams($orderby=id asc)/Observations($orderby=id desc)").list();
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return thingList;
    }


    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(Location) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return Location
     */
    public Location hasLocation(final String filter, final String name) {
        Location location = null;
        Query<Location> query = service.locations().query();

        try {
            EntityList<Location> list;
            list = createFilter(query, filter, name).list();
            if (list.size() == 1) {
                location = list.iterator().next();
            } else {
                log.debug("More than one entity(Location) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(Sensor) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return Sensor
     */
    public Sensor hasSensor(final String filter, final String name) {
        Sensor sensor = null;
        Query<Sensor> query = service.sensors().query();

        try {
            EntityList<Sensor> list;
            list = createFilter(query, filter, name).list();
            if (list.size() == 1) {
                sensor = list.iterator().next();
            } else {
                log.debug("More than one entity(Sensor) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return sensor;
    }

    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(ObservedProperty) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return ObservedProperty
     */
    public ObservedProperty hasObservedProperty(final String filter, final String name) {
        ObservedProperty observedProperty = null;
        Query<ObservedProperty> query = service.observedProperties().query();

        try {
            EntityList<ObservedProperty> list;
            list = createFilter(query, filter, name).list();
            if (list.size() == 1) {
                observedProperty = list.iterator().next();
            } else {
                log.debug("More than one entity(ObservedProperty) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return observedProperty;
    }
    
    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(Datastream) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return Datastream
     */
    public Datastream hasDatastream(final String filter, final String name) {
        Datastream datastream = null;
        Query<Datastream> query = service.datastreams().query();

        try {
            EntityList<Datastream> list;
            list = createFilter(query, filter, name).list();
            if (list.size() == 1) {
                datastream = list.iterator().next();
            } else {
                log.debug("More than one entity(Datastream) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return datastream;
    }
    
    /**
     * 필터를 통해 특정 조건으로 단일의 STA Entity(FeatureOfInterest) 를 검색
     * 
     * @param filter 필터 구문
     * @param name 내용(필터 구문이 없는 경우 기본값으로 사용)
     * @return FeatureOfInterest
     */
    public FeatureOfInterest hasFeatureOfInterest(final String filter, final String name) {
        FeatureOfInterest featureOfInterest = null;
        Query<FeatureOfInterest> query = service.featuresOfInterest().query();

        try {
            EntityList<FeatureOfInterest> list;
            list = createFilter(query, filter, name).list();
            if (list.size() == 1) {
                featureOfInterest = list.iterator().next();
            } else {
                log.debug("More than one entity(FeatureOfInterest) with name " + name);
            }
        } catch (ServiceFailureException e) {
            e.printStackTrace();
        }

        return featureOfInterest;
    }

    /**
     * Thing 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param description 설명
     * @param properties 속성
     * @return Thing
     */
    public Thing createThing(final String filter, final String name, final String description, final Map<String, Object> properties) {
        Thing thing = hasThing(filter, name);

        if (thing == null) {
            thing = new Thing(name, description, properties);
            create(thing);
            log.info("Creating Thing {}.", thing);
        }

        log.info("Using Thing {}.", thing);

        return thing;
    }

    /**
     * Location 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param description 설명
     * @param encodingType 인코딩 방식
     * @param feature 위치 정보(GeoJSON)
     * @return Location
     */
    public Location createLocation(final String filter, final String name, final String description, 
    final String encodingType, Feature feature) {
        Location location = hasLocation(filter, name);

        if(location == null) {
            location = new Location(name, description, encodingType, feature);
            create(location);
            log.info("Creating Location {}.", location);
        }

        log.info("Using Location {}.", location);

        return location;
    }

    /**
     * ObservedProperty 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param definition 정의 방식
     * @param description 설명
     * @return ObservedProperty
     */
    public ObservedProperty createObservedProperty(final String filter, final String name, final String definition, final String description) {
        ObservedProperty observedProperty = hasObservedProperty(filter, name);

        if(observedProperty == null) {
            observedProperty = new ObservedProperty(name, definition, description);
            create(observedProperty);
            log.info("Creating ObservedProperty {}.", observedProperty);
        }

        log.info("Using ObservedProperty {}.", observedProperty);

        return observedProperty;
    }

    /**
     * Sensor 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param description 설명
     * @param encodingType 인코딩 방식
     * @param metadata 메타 정보
     * @return Sensor
     */
    public Sensor createSensor(final String filter, final String name, final String description,
    final String encodingType, final Object metadata) {
        Sensor sensor = hasSensor(filter, name);

        if(sensor == null) {
            sensor = new Sensor(name, description, encodingType, metadata);
            create(sensor);
            log.info("Creating Sensor {}.", sensor);
        }

        log.info("Using Sensor {}.", sensor);

        return sensor;
    }

    /**
     * Datastream 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param description 설명
     * @param observationType 관측자료 유형
     * @param unitOfMeasurement 측정 단위
     * @param thing 연관된 STA Thing Entity
     * @param sensor 연관된 STA Sensor Entity
     * @param observedProperty 연관된 STA ObservedProperty Entity
     * @return Datastream
     */
    public Datastream createDatastream(final String filter, final String name, final String description, final String observationType, final UnitOfMeasurement unitOfMeasurement, final Thing thing, final Sensor sensor, final ObservedProperty observedProperty) {
        Datastream datastream = hasDatastream(filter, name);

        if(datastream == null) {
            datastream = new Datastream(name, description, observationType, unitOfMeasurement);
			datastream.setThing(thing);
			datastream.setSensor(sensor);
			datastream.setObservedProperty(observedProperty);
            create(datastream);
            log.info("Creating Datastream {}.", datastream);
        }

        log.info("Using Datastream {}.", datastream);

        return datastream;
    }

    /**
     * FeatureOfInterest 생성
     * 
     * @param filter 검색 조건
     * @param name 이름
     * @param description 설명
     * @param encodingType 인코딩 방식
     * @param feature 관심 지역 정보(GeoJSON)
     * @return FeatureOfInterest
     */
    public FeatureOfInterest createFeatureOfInterest(final String filter, final String name, final String description, final String encodingType, Feature feature) {
        FeatureOfInterest featureOfInterest = hasFeatureOfInterest(filter, name);

        if(featureOfInterest == null) {
            featureOfInterest = new FeatureOfInterest(name, description, encodingType, feature);
            create(featureOfInterest);
            log.info("Creating FeatureOfInterest {}.", featureOfInterest);
        }

        log.info("Using FeatureOfInterest {}.", featureOfInterest);

        return featureOfInterest;
    }

    /**
     * Observation 생성
     * 
     * @param result 관측 결과값
     * @param resultTime 관측 결과값 산출 날짜/시간
     * @param phenomenonTime 관측 날짜/시간
     * @param interval 관측 시간 구간(초)
     * @param dataStream STA Datastream Entity
     * @param featureOfInterest STA DatFeatureOfInterestastream Entity
     * @return Observation
     */
    public Observation createObservation(final Object result, final ZonedDateTime resultTime, final ZonedDateTime phenomenonTime, final long interval, final Datastream dataStream, final FeatureOfInterest featureOfInterest) {
        Observation observation = new Observation();
        observation.setResult(result);
        observation.setResultTime(resultTime);
        observation.setDatastream(dataStream);
        observation.setFeatureOfInterest(featureOfInterest);

        ZonedDateTime intervalTime = phenomenonTime;
        if(intervalTime == null) {
            intervalTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        }

        if(interval == 0) {
            observation.setPhenomenonTimeFrom(intervalTime);
        } else {
            ZonedDateTime zdtStart = null;
            ZonedDateTime zdtEnd = null;
            if (interval > 0) {
                zdtStart = intervalTime;
                zdtEnd = zdtStart.plusSeconds(interval);
            } else {
                zdtEnd = intervalTime;
                zdtStart = zdtEnd.plusSeconds(interval);
            }

            observation.setPhenomenonTimeFrom(Interval.of(zdtStart.toInstant(), zdtEnd.toInstant()));
        }
        
        create(observation);
        log.info("Creating Observation {}.", observation);

        return observation;
    }

    /**
     * 입력된 location 을 조회하여 없는 경우 해당 thing 의 location 값을 업데이트
     * @param thing STA Thing Entity
     * @param location STA Location Entity
     * @return 업데이트 여부
     */
    public boolean updateThingWithLocation(Thing thing, Location location) {
        boolean found = false;
        List<Location> locations = thing.getLocations().toList();

        for (Location loc : locations) {
            if (loc.getId().equals(location.getId())) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            thing.getLocations().clear();
            thing.getLocations().add(location.withOnlyId());
            update(thing);
        }

        return !found;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }
}
