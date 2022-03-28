package guru.springframework.msscbeerservice.services.brewing;

import guru.springframework.msscbeerservice.config.JmsConfig;
import guru.springframework.msscbeerservice.domain.Beer;
import guru.springframework.msscbeerservice.events.BrewBeerEvent;
import guru.springframework.msscbeerservice.repositories.BeerRepository;
import guru.springframework.msscbeerservice.services.inventory.BeerInventoryService;
import guru.springframework.msscbeerservice.web.mappers.BeerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrewingService {
    private final BeerRepository beerRepository;
    private final BeerInventoryService beerInventoryService;
    private final JmsTemplate jmsTemplate;
    private final BeerMapper beerMapper;

    @Scheduled(fixedRate=5000)
    public void checkForLowInventory(){
        List<Beer> beers = this.beerRepository.findAll();

        beers.forEach(
                beer -> {
                    Integer inventoryQOH = this.beerInventoryService.getOnhandInventory(beer.getId());
                    log.debug("Min on hand is: " + beer.getMinOnHand());
                    log.debug("Inventory is : " + beer.getMinOnHand());

                    if(beer.getMinOnHand() >= inventoryQOH){
                        this.jmsTemplate.convertAndSend(JmsConfig.BREWING_REQUEST_QUEUE, new BrewBeerEvent(this.beerMapper.beerToBeerDto(beer)));
                    }
                }
        );
    }


}
