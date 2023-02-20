package uk.co.acta.awsidp.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Keys {
   List<Key> keys = new ArrayList<>();


   public void addKey(Key key) {
       keys.add(key);
   }
}
