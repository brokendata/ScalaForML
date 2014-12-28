/**
 * Copyright (c) 2013-2015  Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" 
 * ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.97.2
 */
package org.scalaml.reinforcement.qlearning

import org.scalaml.core.Matrix




		/**
		 * <p>Enumerator to define the type of a parameters used to update the policy during
		 * the training of Q-learning model.</p>
		 */
object QLDataVar extends Enumeration {
	type QLDataVar = Value
	val REWARD, PROB, VALUE = Value
}


		/**
		 * <p>Class that encapsulate the attributes of the policy in Q-learning algorithm</p>
		 * @author Patrick Nicolas
		 * @since January 25, 2014
		 * @note Scala for Machine Learning Chap 11 Reinforcement learning/Q-learning
		 */
final protected class QLData {
	import QLDataVar._
	var reward: Double = 1.0
	var probability: Double = 1.0
	var value: Double = 0.0
	
		/**
		 * Compute the overall value for an action adjusted by its probability
		 * @return adjusted value
		 */
	@inline
	final def estimate: Double = value*probability
	
		/**
		 * Select the attribute of an element of Q-learning policy using its type
		 * @param type of the attribute {REWARD, PROBABILITY, VALUE}
		 * @return value of this attribute
		 */
	final def value(varType: QLDataVar): Double = varType match {
		case REWARD => reward
		case PROB => probability
		case VALUE => value
	}
	
	override def toString: String =s"\nValue= $value Reward= $reward Probability= $probability"
}


		/**
		 * <p>Class that defines the policy for a given set of input and a number of states.</p>
		 * @constructor Create a policy as a model for Q-learning. 
		 * @throws IllegalArgumentException if the class parameters are undefined
		 * @param numStates Number of states for this policy.
		 * @param input Input to initialize the policy.
		 * @see org.scalaml.design.Model
		 * 
		 * @author Patrick Nicolas
		 * @since January 25, 2014
		 * @note Scala for Machine Learning Chap 11 Reinforcement learning/Q-learning
		 */
final protected class QLPolicy[T](numStates: Int, input: Array[QLInput]) {
	import QLDataVar._,  QLPolicy._
	check(numStates, input)
	
		/**
		 * Initialization of the policy using the input probabilities and rewards
		 */
	val qlData = {
		val data = Array.tabulate(numStates)(v => Array.fill(numStates)(new QLData))
		input.foreach(in => {  
			data(in.from)(in.to).reward = in.reward
			data(in.from)(in.to).probability = in.prob
		})
		data
	}

		/**
		 * <p>Set the Q value for an action from state from to state to</p>
		 * @param from Source state for the action for which the value is updated
		 * @param to destination state for the action for which the value is updated
		 * @throws IllegalArgumentException if the source or destination states are out of range
		 */
	def setQ(from: Int, to: Int, value: Double): Unit = {
		require(from >= 0 && from < qlData.size, s"QLPolicy.setQ source state $from is out of range")
		require(to >= 0 && from < qlData(0).size, s"QLPolicy.setQ source state $to is out of range")
		qlData(from)(to).value = value
	}
   
		/**
		 * Retrieve the Q-value for an state transition action from 'state' from to state 'to'
		 * @param from Source state for the action
		 * @param to destination state for the action
		 * @throws IllegalArgumentException if the source or destination states are out of range
		 */
	final def Q(from: Int, to: Int): Double = {
		require(from >= 0 && from < qlData.size, s"QLPolicy.Q source state $from is out of range")
		require(to >= 0 && from < qlData(0).size, s"QLPolicy.Q source state $to is out of range")
		qlData(from)(to).value
	}
	
		/**
		 * Retrieve the estimate for an state transition action from 'state' from to state 'to'
		 * @param from Source state for the action
		 * @param to destination state for the action
		 * @throws IllegalArgumentException if the source or destination states are out of range
		 */
	final def EQ(from: Int, to: Int): Double = {
		require(from >= 0 && from < qlData.size, s"QLPolicy.EQ source state $from is out of range")
		require(to >= 0 && from < qlData(0).size, s"QLPolicy.EQ source state $to is out of range")
		qlData(from)(to).estimate
	}
 
		/**
		 * Retrieve the reward for an state transition action from 'state' from to state 'to'
		 * @param from Source state for the action
		 * @param to destination state for the action
		 * @throws IllegalArgumentException if the source or destination states are out of range
		 */
	final def R(from: Int, to: Int): Double = {
		require(from >= 0 && from < qlData.size, s"QLPolicy.R source state $from is out of range")
		require(to >= 0 && from < qlData(0).size, s"QLPolicy.R source state $to is out of range")
		qlData(from)(to).reward
	}
   
		/**
		 * Retrieve the probability for an state transition action from 'state' from to state 'to'
		 * @param from Source state for the action
		 * @param to destination state for the action
		 * @throws IllegalArgumentException if the source or destination states are out of range
		 */
	final def P(from: Int, to: Int): Double = {
		require(from >= 0 && from < qlData.size, s"QLPolicy.P source state $from is out of range")
		require(to >= 0 && from < qlData(0).size, s"QLPolicy.P source state $to is out of range")
		qlData(from)(to).probability
	}
   
	override def toString: String = s" Reward\n${toString(REWARD)}"

	def toString(varType: QLDataVar): String = { 
		val buf = new StringBuilder
		Range(1, numStates).foreach(i => {
			val line = qlData(i).zipWithIndex
						.foldLeft(new StringBuilder)((b, qj) => b.append(f"${qj._1.value(varType)}%2.2f, ") )
			line.setCharAt(line.size-1, '\n')
			buf.append(line.toString)
		})
		buf.toString
	} 
}


		/**
		 * Companion object to the QLPolicy class of Q-learning algorithm. The purpose of this
		 * singleton is to validate the class parameters and define its constructor
		 * 
		 * @author Patrick Nicolas
		 * @since January 25, 2014
		 * @note Scala for Machine Learning Chap 11 Reinforcement learning/Q-learning
		 */
object QLPolicy {
		/**
		 * Default constructor for a Qlearning policy
		 * @param numStates Number of states for this policy.
		 * @param input Input (rewards and probability) to initialize the policy.
		 */
	def apply[T](numStates: Int, input: Array[QLInput]): QLPolicy[T] = 
			new QLPolicy[T](numStates, input)

	private val MAX_NUM_STATES = 2048

	protected def check(numStates: Int, input: Array[QLInput]): Unit = {
		require(numStates >0 && numStates < MAX_NUM_STATES, 
				s"QLPolicy.check Number of states $numStates is out of range")
		require( !input.isEmpty, 
				"QLPolicy.check the input to the Q-leaning policy is undefined")
		require(input.size < MAX_NUM_STATES, 
				s"QLPolicy.check, the size of the input ${input.size} is out of range" )
	}
}


// -----------------------------  EOF -----------------------------------