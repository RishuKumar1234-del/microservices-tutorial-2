package com.lcwd.userservice.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lcwd.userservice.entities.Hotel;
import com.lcwd.userservice.entities.Rating;
import com.lcwd.userservice.entities.UserEntity;
import com.lcwd.userservice.exceptions.ResourceNotFoundException;

import com.lcwd.userservice.repositories.UserRepository;


@Service
public class UserviceImple implements UserServices {
	
	@Autowired
	private RestTemplate restTemplate;


	private Logger logger=LoggerFactory.getLogger(UserviceImple.class);

	private UserRepository  userRepository;
	
	@Autowired
	public UserviceImple(UserRepository userRepository) {
		this.userRepository=userRepository;
	   }

	@Override
	public UserEntity saveUser(UserEntity user) {
		
		//generate Unique userid
		 
		
		return userRepository.save(user);
	}

	@Override
	public List<UserEntity> getAllUser() {
		
		return userRepository.findAll();
	}

	@Override
	public UserEntity getUser(Integer uid) {
		
		//get user from the database with the help of user repository
		UserEntity user= userRepository.findById(uid).orElseThrow(()->new ResourceNotFoundException("user with given "
				+ "id is not found on the server" +uid));
		
	//fetch rating of the above user from the Rating Services
		//http://localhost:8084/Rating/users/3
		
		Rating[] ratingfromuser = restTemplate.getForObject("http://localhost:8084/Rating/users/"
		+user.getUserId(),Rating[].class );
		logger.info("{} ",ratingfromuser);
		
		List<Rating> ratings = Arrays.stream(ratingfromuser).toList();
		 
		List<Rating> ratingList=ratings.stream().map(rating->{
			//api call to hotel service to get the hotel:( http://localhost:8082/Hotel/hotel/2 )
			 ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://localhost:8082/Hotel/hotel/"+rating.getHotelid(), Hotel.class);
			 Hotel hotel = forEntity.getBody();
			 logger.info("response status code: {} ",forEntity.getStatusCode());
			//set hotel to to rating
			 rating.setHotel(hotel);
			//return rating
			return rating;
		}).collect(Collectors.toList());
				
		
		user.setRatings(ratingList);
		
		return user;
	}

}
