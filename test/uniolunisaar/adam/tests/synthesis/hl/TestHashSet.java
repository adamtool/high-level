package uniolunisaar.adam.tests.synthesis.hl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestHashSet {

    private class TestObj {

        private String id;

        public TestObj(TestObj obj) {
            this.id = obj.id;
        }

        public TestObj(String id) {
            this.id = id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestObj other = (TestObj) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return id;
        }

    }

    @Test
    public void testHashSet() {
        Set<TestObj> set = new HashSet<>();
        set.add(new TestObj("asdf"));
        set.add(new TestObj("lorem"));
        Assert.assertTrue(set.contains(new TestObj("asdf")));

        Set<TestObj> setCopy = new HashSet<>();
        for (TestObj obj : set) {
            setCopy.add(new TestObj(obj));
        }
        Assert.assertTrue(setCopy.contains(new TestObj("asdf")));

        TestObj test = new TestObj("asdf");
        Assert.assertTrue(setCopy.contains(test));
        test.setId("lorem");
        Assert.assertTrue(setCopy.contains(test));
        test.setId("peter");
        Assert.assertFalse(setCopy.contains(test));
        
        setCopy.add(test);
        Assert.assertTrue(setCopy.contains(test));
        test.setId("klaus");
        Assert.assertFalse(setCopy.contains(test));
        boolean isEqual = false;
        for (TestObj testObj : setCopy) {
            if(testObj.equals(test)) {
                isEqual = true;
            }
        }
        Assert.assertTrue(isEqual); // IT is a problem to change the fields of hashed elements, because the hash changes but it is not updated by the set
        Assert.assertFalse(setCopy.contains(new TestObj("peter")));
        

    }

    @Test
    public void testHashSetColoredTransitions() {
        HLPetriGame game = new HLPetriGame("test");
        Set<ColoredTransition> set = new HashSet<>();
        Valuation v1 = new Valuation();
        v1.put(new Variable("x"), new Color("c"));
        set.add(new ColoredTransition(game, game.createTransition("asdf"), v1));
        Valuation v2 = new Valuation();
        v2.put(new Variable("x1"), new Color("c1"));
        set.add(new ColoredTransition(game, game.createTransition("lorem"), v2));

        Valuation v1copy = new Valuation(v1);
        Assert.assertTrue(set.contains(new ColoredTransition(game, game.getTransition("asdf"), v1copy)));

        Set<ColoredTransition> setCopy = new HashSet<>();
        for (ColoredTransition obj : set) {
            setCopy.add(new ColoredTransition(obj));
        }

        Valuation v1Second = new Valuation();
        v1Second.put(new Variable("x"), new Color("c"));
        Assert.assertEquals(v1Second, v1);
        Assert.assertEquals(v1Second, v1copy);
        Assert.assertTrue(setCopy.contains(new ColoredTransition(game, game.getTransition("asdf"), v1Second)));

    }

    @Test
    public void testHashCode() {
        HLPetriGame game = new HLPetriGame("test");
        Set<ColoredTransition> set1 = new HashSet<>();
        Valuation v1 = new Valuation();
        v1.put(new Variable("x"), new Color("c"));
        set1.add(new ColoredTransition(game, game.createTransition("asdf"), v1));
        Valuation v2 = new Valuation();
        v2.put(new Variable("x1"), new Color("c1"));
        set1.add(new ColoredTransition(game, game.createTransition("lorem"), v2));

        Set<ColoredTransition> set2 = new HashSet<>();
        set2.add(new ColoredTransition(game, game.getTransition("lorem"), v1));
        set2.add(new ColoredTransition(game, game.getTransition("asdf"), v2));

        Assert.assertFalse(set1.hashCode() == set2.hashCode());

    }
}
