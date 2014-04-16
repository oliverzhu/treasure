package com.home.rtti;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public class LiteralPetCreator extends PetCreator {
	//不可修改的集合
	public static final List<Class<? extends Pet>> allTypes = 
			Collections.unmodifiableList(Arrays.asList(
					Pet.class,Dog.class,Cat.class,Rodent.class,
					Mutt.class,Pug.class,Egyptianmau.class,Manx.class,
					Cymric.class,Rat.class,Mouse.class,Hamster.class));
	
	private static final List<Class<? extends Pet>> types = 
			allTypes.subList(allTypes.indexOf(Mutt.class), 
					allTypes.size());

	@Override
	public List<Class<? extends Pet>> types() {
		return types;
	}
	

}
