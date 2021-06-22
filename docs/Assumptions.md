# Assumptions:

### 1st Stage Assumptions:
| ID        | System Assumption|          
| ----------|:---------|
|1| Assume that there is a single server that always behaves honestly |
|2| There is a Public Key Infrastructure in place, that performs the distribution of keys among all participants before the start of the system;There are no Sybil attacks |
|3| The usage of a localized broadcast communication medium, such as Bluetooth, will be emulated via the grid. At each epoch, a user consults the grid to determine which other users are nearby |
|4| The space is represented by a bi-dimensional grid of a suitable dimension |
|5| Students are free to define the size of the grid, the number of users, and the nearby function as appropriate. |
|6| Students can pre-generate a sequence of grid assignments, one per epoch, before starting the system. |
|7| Each epoch is represented by one and only one grid. |
|8| Users are placed, at each epoch, in a given location in the grid. Students are free to place users in the grid at random or using some static assignment. |
|9| Correct users do not change their location in the same epoch;  Byzantine users can behave arbitrarily. |
|10| There can be up to *f* byzantine users in the entire system. For simplicity, there is a limit, *`f’<f`*, on the number of byzantine users that can be (or appear to be) nearby a correct user |
|11| An attacker can drop, reject, manipulate and duplicate messages |
|12| The communication channels are not secured, in particular solutions relying on secure channel technologies such as TLS are not allowed |
