package ca.ulaval.glo4002.game.domain.dinosaur.herd;

import ca.ulaval.glo4002.game.domain.dinosaur.*;
import ca.ulaval.glo4002.game.domain.dinosaur.consumption.FoodConsumptionStrategy;
import ca.ulaval.glo4002.game.domain.dinosaur.consumption.FoodNeed;
import ca.ulaval.glo4002.game.domain.dinosaur.sumoFight.SumoFightOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HerdTest {

    private final static String DINOSAUR_NAME = "Bobi";
    private final static String ANOTHER_DINOSAUR_NAME = "Alyce";
    private final static String NON_EXISTING_DINOSAUR_NAME = "Alfred";
    private final static int DINOSAUR_WEIGHT = 80;
    private final static int ANOTHER_DINOSAUR_WEIGHT = 100;

    private FoodConsumptionStrategy dinosaurStrategy;
    private FoodConsumptionStrategy anotherDinosaurStrategy;
    private Dinosaur aDinosaurInHerd;
    private Dinosaur anotherDinosaurInHerd;
    private Dinosaur aDinosaur;
    private Dinosaur anotherDinosaur;
    private final List<Dinosaur> dinosaurs = new ArrayList<>();
    private DinosaurFeeder aDinosaurFeeder;
    private DinosaurFeeder anotherDinosaurFeeder;
    private SumoFightOrganizer sumoFightOrganizer;
    private Herd herd;

    @BeforeEach
    void setUp() {
        dinosaurStrategy = mock(FoodConsumptionStrategy.class);
        anotherDinosaurStrategy = mock(FoodConsumptionStrategy.class);

        aDinosaurInHerd = new Dinosaur(Species.Allosaurus, DINOSAUR_WEIGHT, DINOSAUR_NAME,
                Gender.M, dinosaurStrategy, DinosaurStage.ADULT);
        anotherDinosaurInHerd = new Dinosaur(Species.TyrannosaurusRex, ANOTHER_DINOSAUR_WEIGHT,
                ANOTHER_DINOSAUR_NAME, Gender.M, anotherDinosaurStrategy, DinosaurStage.ADULT);
        dinosaurs.add(aDinosaurInHerd);
        dinosaurs.add(anotherDinosaurInHerd);

        aDinosaur = mock(Dinosaur.class);
        anotherDinosaur = mock(Dinosaur.class);

        sumoFightOrganizer = mock(SumoFightOrganizer.class);
        aDinosaurFeeder = mock(DinosaurFeeder.class);
        anotherDinosaurFeeder = mock(DinosaurFeeder.class);
        herd = new Herd(dinosaurs, sumoFightOrganizer, List.of(aDinosaurFeeder, anotherDinosaurFeeder));
    }

    @Test
    public void givenADinosaurWithNameNotAlreadyExisting_addDinosaur_thenDinosaurShouldBeAdded() {
        Dinosaur aDinosaurWithNonExistingName = new Dinosaur(Species.Allosaurus, DINOSAUR_WEIGHT,
                NON_EXISTING_DINOSAUR_NAME, Gender.M, dinosaurStrategy, DinosaurStage.ADULT);

        herd.addDinosaur(aDinosaurWithNonExistingName);

        assertTrue(dinosaurs.contains(aDinosaurWithNonExistingName));
    }

    @Test
    public void givenADinosaurWithNameAlreadyExisting_whenAddDinosaur_thenDinosaurShouldNotBeAdded() {
        Dinosaur aDinosaurWithExistingName = new Dinosaur(Species.Allosaurus, DINOSAUR_WEIGHT,
                DINOSAUR_NAME, Gender.M, dinosaurStrategy, DinosaurStage.ADULT);

        herd.addDinosaur(aDinosaurWithExistingName);

        assertFalse(dinosaurs.contains(aDinosaurWithExistingName));
    }

    @Test
    public void givenANameOfANonExistingDinosaurInHerd_whenHasDinosaurWithName_thenNameShouldNotExist() {
        boolean dinosaurNameExists = herd.hasDinosaurWithName(NON_EXISTING_DINOSAUR_NAME);

        assertFalse(dinosaurNameExists);
    }

    @Test
    public void givenANameOfAnExistingDinosaurInHerd_whenHasDinosaurWithName_thenNameShouldExist() {
        boolean dinosaurNameExists = herd.hasDinosaurWithName(DINOSAUR_NAME);

        assertTrue(dinosaurNameExists);
    }

    @Test
    public void givenANameOfANonExistingDinosaurInHerd_whenGetDinosaurWithName_thenShouldThrowNonExistentNameException() {
        assertThrows(NonExistentNameException.class, ()->herd.getDinosaurWithName(NON_EXISTING_DINOSAUR_NAME));
    }

    @Test
    public void givenANameOfAnExistingDinosaurInHerd_whenGetDinosaurWithName_thenDinosaurShouldBeFound() {
        Dinosaur dinosaurWithTheName = herd.getDinosaurWithName(DINOSAUR_NAME);

        assertEquals(aDinosaurInHerd, dinosaurWithTheName);
    }

    @Test
    public void givenHerd_whenFeedDinosaurs_thenOnlyFastingDinosaursShouldBeRemoved() {
        when(dinosaurStrategy.areFoodNeedsSatisfied()).thenReturn(true);
        when(dinosaurStrategy.getStarvingFoodNeeds(anyInt())).thenReturn(new ArrayList<>());
        when(anotherDinosaurStrategy.areFoodNeedsSatisfied()).thenReturn(false);
        when(anotherDinosaurStrategy.getStarvingFoodNeeds(anyInt())).thenReturn(new ArrayList<>());

        herd.feedDinosaurs();

        assertTrue(dinosaurs.contains(aDinosaurInHerd));
        assertFalse(dinosaurs.contains(anotherDinosaurInHerd));
    }

    @Test
    public void giveDinosaurFoodNeeds_whenFeedDinosaurs_thenAllDinosaurFeederShouldFeed() {
        List<FoodNeed> dinosaurFoodNeeds = new ArrayList<>();
        List<FoodNeed> anotherDinosaurFoodNeeds = new ArrayList<>();
        when(dinosaurStrategy.getStarvingFoodNeeds(anyInt())).thenReturn(dinosaurFoodNeeds);
        when(anotherDinosaurStrategy.getStarvingFoodNeeds(anyInt())).thenReturn(anotherDinosaurFoodNeeds);

        herd.feedDinosaurs();

        ArgumentMatcher<Map<Dinosaur, List<FoodNeed>>> isCorrectDinosaursWithNeed =
                map->map.get(aDinosaurInHerd).equals(dinosaurFoodNeeds)
                        && map.get(anotherDinosaurInHerd).equals(anotherDinosaurFoodNeeds);
        verify(aDinosaurFeeder).feedDinosaurs(argThat(isCorrectDinosaursWithNeed));
        verify(anotherDinosaurFeeder).feedDinosaurs(argThat(isCorrectDinosaursWithNeed));
    }

    @Test
    public void givenTwoDinosaursInHerd_whenOrganizeSumoFight_thenSumoFightShouldBeCalled() {
        herd.sumoFight(aDinosaurInHerd, anotherDinosaurInHerd);

        verify(sumoFightOrganizer).sumoFight(aDinosaurInHerd, anotherDinosaurInHerd);
    }

    @Test
    public void givenTwoDinosaursInHerd_whenOrganizeSumoFight_thenWinnerDinosaurShouldWinFight() {
        when(sumoFightOrganizer.sumoFight(aDinosaur, anotherDinosaur))
                .thenReturn(List.of(aDinosaur));

        herd.sumoFight(aDinosaur, anotherDinosaur);

        verify(aDinosaur).winFight();
    }

    @Test
    public void givenTwoDinosaursInHerd_whenOrganizeSumoFight_thenDinosaurNotWinningShouldLoseFight() {
        when(sumoFightOrganizer.sumoFight(aDinosaur, anotherDinosaur))
                .thenReturn(List.of(aDinosaur));

        herd.sumoFight(aDinosaur, anotherDinosaur);

        verify(anotherDinosaur).loseFight();
    }

    @Test
    public void whenResetSumoFight_thenResetOfSumoFightOrganizerShouldBeCalled() {
        herd.resetSumoFight();

        verify(sumoFightOrganizer).reset();
    }

    @Test
    public void givenTwoDinosaursInHerd_whenPredictWinnerSumoFight_thenScheduleSumoFightOfSumoFightOrganizerShouldBeCalled() {
        herd.predictWinnerSumoFight(aDinosaurInHerd, anotherDinosaurInHerd);

        verify(sumoFightOrganizer).scheduleSumoFight(aDinosaurInHerd, anotherDinosaurInHerd);
    }

    @Test
    public void whenReset_thenHerdShouldBeEmpty() {
        herd.reset();

        assertTrue(dinosaurs.isEmpty());
    }

    @Test
    public void whenGetAllDinosaurs_thenAllDinosaursShouldBeReturned() {
        List<Dinosaur> allDinosaursInHerd = herd.getAllDinosaurs();

        assertEquals(dinosaurs, allDinosaursInHerd);
    }

    @Test
    public void whenIncreasingBabiesWeight_thenTheBabyDinosaursShouldIncreaseTheirWeight() {
        when(aDinosaur.getName()).thenReturn("name");
        herd.addDinosaur(aDinosaur);
        when(anotherDinosaur.getName()).thenReturn("mane");
        herd.addDinosaur(anotherDinosaur);

        herd.increasingBabiesWeight();

        verify(aDinosaur).increaseWeight();
        verify(anotherDinosaur).increaseWeight();
    }
}
