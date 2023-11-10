import org.example.CSVParser;
import org.example.DoParse;
import org.example.JSONParser;
import org.example.SuaferParser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;
public class ParserTest {


    SuaferParser csvParser = new CSVParser();
    SuaferParser jsonParser = new JSONParser();
    User user1;
    Weapon weapon;

    @Before
    public void defoult(){
        user1 = new User("Ivan", 12);
        weapon = new Weapon("Меч",10,new String[]{"поджигание","кровотёк"},98);

        csvParser.parseObject(user1,"user.csv");
        jsonParser.parseObject(weapon,"weapon.json");
    }

    @Test
    public void testCSVWRITE() {
        File file = new File("user.csv");
        try {
            assertNotEquals(0, Files.size(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testJSONWRITE() {
        File file = new File("weapon.json");
        try {
            assertNotEquals(0, Files.size(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testJSONREAD() {
        Weapon whatIRead;
        try{
            whatIRead = (Weapon) jsonParser.parseFile("weapon.json", Weapon.class);
            if(weapon!=null){
                assertEquals("Меч", whatIRead.getName());
                assertEquals(10, whatIRead.getDamage());
                assertEquals(2, whatIRead.getEffect().length);
                assertEquals(98, whatIRead.getStrength());
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @DoParse
    private class Weapon{
        private String name;
        private double damage;
        private String[] effect;
        private int strength;

        public Weapon(){}

        public Weapon(String name, int damage, String[] effect, int strength) {this.name = name;this.damage = damage;this.effect = effect;this.strength = strength;}

        public String getName() {
            return name;
        }

        public double getDamage() {
            return damage;
        }

        public String[] getEffect() {
            return effect;
        }

        public int getStrength() {
            return strength;
        }
    }

    @DoParse
    private class User{
        private String name;
        private int age;

        public User(){}

        public User(String name, int age) {this.name = name;this.age = age;}
    }
}
