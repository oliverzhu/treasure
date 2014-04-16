package com.home.rtti;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.home.common.MapData;
import com.home.testbean.Cat;
import com.home.testbean.Cymric;
import com.home.testbean.Dog;
import com.home.testbean.Egyptianmau;
import com.home.testbean.Hamster;
import com.home.testbean.Manx;
import com.home.testbean.Mouse;
import com.home.testbean.Mutt;
import com.home.testbean.Pet;
import com.home.testbean.Pug;
import com.home.testbean.Rat;
import com.home.testbean.Rodent;

public class PetCount {
	
	/*public static final List<Class<? extends Pet>> allTypes = 
	Collections.unmodifiableList(Arrays.asList(
			Pet.class,Dog.class,Cat.class,Rodent.class,
			Mutt.class,Pug.class,Egyptianmau.class,Manx.class,
			Cymric.class,Rat.class,Mouse.class,Hamster.class));
	*/
	static class PetCounter extends LinkedHashMap<Class<? extends Pet>, Integer>
	{
		public PetCounter()
		{
			super(MapData.map(LiteralPetCreator.allTypes, 0));
		}
		
		/**
		 * 统计基类和导出类的个数
		 * @param pet
		 */
		public void count(Pet pet)
		{
			for(Map.Entry<Class<? extends Pet>, Integer> pair:
				entrySet())
			{
				if(pair.getKey().isInstance(pet))
				{
					put(pair.getKey(),pair.getValue() + 1);
				}
			}
		}
		
		public String toString()
		{
			StringBuilder result = new StringBuilder("{");
			for(Map.Entry<Class<? extends Pet>, Integer> pair:
				entrySet())
			{
				result.append(pair.getKey().getSimpleName());
				result.append("=");
				result.append(pair.getValue());
				result.append(", ");
			}
			
			result.delete(result.length() - 2, result.length());
			result.append("}");
			return result.toString();
		}
	}
	
	public static void main(String[] args) {
		PetCounter petCount = new PetCounter();
		for(Pet pet : Pets.createArray(20))
		{
			System.out.print(pet.getClass().getSimpleName() + "  ");
			//传入具体实例
			petCount.count(pet);
		}
		System.out.println();
		System.out.print(petCount);
	}
}
