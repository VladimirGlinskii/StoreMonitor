# Cashier Simulator

## Requirements

- Each cash register must operate for at least one hour per day.
- Each cashier must work at least one hour per day.

## Algorithm

For each store once every 20 minutes do the following:

### Preparation

1. Receive all store cashiers ordered by their activity during the current day
   in ascending order.
2. Receive all store cash registers with their sessions of the current day.

### Simulation

1. While next cash register in the store exists do:
   1. If cash register is opened less than hour go to step 1.
   2. If there are free cashiers, take cashier with the least activity for the current day
      and open the cash register for him.
   3. If there are no free cashiers and cash register is opened, 
      close it with the probability of 0.25.
2. END