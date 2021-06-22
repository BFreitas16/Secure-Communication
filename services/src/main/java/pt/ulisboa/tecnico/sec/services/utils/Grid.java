package pt.ulisboa.tecnico.sec.services.utils;

import pt.ulisboa.tecnico.sec.services.exceptions.OutOfEpochException;

import java.util.ArrayList;
import java.util.List;

public class Grid {
	
	private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 3;

	// simulation of users
    private static final int user1   = 1;
    private static final int user2   = 2;
    private static final int byzUser = 3;
    private static final int user4   = 4;

    private static final int[][][] locationsAtEpochs = {
    	{   // location for epoch 1
    		// only possible to prove the location of user1 and byzUser -> server will accept it (f' < 2f + 1)
    		{0,     byzUser, user2},
    		{user4, user1,   0},
    		{0,     0,       0}
    	},{ // location for epoch 2
    		// only possible to prove the location of user1 and byzUser -> server will accept it (f' < 2f + 1)
    		{0,     byzUser, 0},
    		{user4, user1,   user2},
    		{0,     0,       0}
    	},{ // location for epoch 3
    		// only possible to prove the location of user4 -> server will accept it (f' < 2f + 1)
    		{user1, byzUser, 0},
    		{user4, 0,       0},
    		{user2, 0,       0}
    	},{ // location for epoch 4
    		// only possible to prove the location of byzUser -> server will accept it (f' < 2f + 1)
    		{0, user1,   user2},
    		{0, byzUser, 0},
    		{0, user4,   0}
    	},{ // location for epoch 5
    		// impossible to prove the location -> server will not accept it
    		{0, user1, byzUser},
    		{0, 0,     0},
    		{0, 0,     0}
    	}, { // location for epoch 6
    		// impossible to prove the location -> server will not accept it
			{0, user1, 0},
			{0, 0, 0},
			{0, 0, 0}
    	}
    };


    public static void main(String[] args) throws OutOfEpochException {
        printGridAtEpoch(3);
        List<Integer> users = getUsersInRangeAtEpoch(1, 3, 1);
        users.forEach( x -> System.out.println(x) );
    }


    public static void printGridAtEpoch(int epoch){

    	try {
			int[][] locationsAtEpoch = getLocationAtEpoch(epoch);

			for(int x = 0; x < locationsAtEpoch.length; x++){
	            for (int y = 0; y < locationsAtEpoch.length; y++){
	                System.out.print(locationsAtEpoch[x][y]+"\t");
	            }
	            System.out.println();
	        }

		} catch (ArrayIndexOutOfBoundsException | OutOfEpochException e) {
			e.printStackTrace(); // Pokeball style
		}

    }


    public static int[] getLocationOfUserAtEpoch(int user, int epoch) throws OutOfEpochException {
    	int[][] locationsAtEpoch = getLocationAtEpoch(epoch);

    	for (int i = 0; i < locationsAtEpoch.length; i++) {
    		for (int j = 0; j < locationsAtEpoch[i].length; j++) {
    			if (locationsAtEpoch[i][j] == user)
    				return new int[] {i, j};
    		}
    	}

    	return new int[] {-1, -1};
    }


    public static List<Integer> getUsersInRangeAtEpoch(int user, int epoch, int range) throws OutOfEpochException {
    	int[][] locationsAtEpoch = getLocationAtEpoch(epoch);

    	int[] myLocation = getLocationOfUserAtEpoch(user, epoch);
    	int x = myLocation[0];
    	int y = myLocation[1];

    	List<Integer> users = new ArrayList<>();

    	if (myLocation[0] < 0) return users;

    	for (int i = 1; i <= range; i++) {
    		verifyingCompassRosePositions(locationsAtEpoch, users, x, y, i);
    	}

    	return users;
    }


    /***********************************************************************************************/
	/*                                     Auxiliary Functions                                     */
	/***********************************************************************************************/


    private static int[][] getLocationAtEpoch(int epoch) throws OutOfEpochException {
    	if(epoch > locationsAtEpochs.length)
    		throw new OutOfEpochException("There are no more epochs to access.");
    	return locationsAtEpochs[epoch - 1];
    }

    public static int numberOfEpochs() {
    	return locationsAtEpochs.length;
	}

    /**
     *
     * @param locationsAtEpoch - all the positions
     * @param result - the result list
     * @param x - the row of the user
     * @param y - the column of the user
     * @param i - the range to verify
     */
    private static void verifyingCompassRosePositions(int[][] locationsAtEpoch,
    		List<Integer> result, int x, int y, int i) {

    	// verifying the E position
    	if (y + i < NUM_COLS && locationsAtEpoch[x][y + i] != 0) {
    		result.add(locationsAtEpoch[x][y + i]);
    	}

    	// verifying the W column
    	if (y - i >= 0 && locationsAtEpoch[x][y - i] != 0) {
    		result.add(locationsAtEpoch[x][y - i]);
    	}

    	// verifying the S column
    	if (x + i < NUM_ROWS && locationsAtEpoch[x + i][y] != 0) {
    		result.add(locationsAtEpoch[x + i][y]);
    	}

    	// verifying the N column
    	if (x - i >= 0 && locationsAtEpoch[x - i][y] != 0) {
    		result.add(locationsAtEpoch[x - i][y]);
    	}

    	// verifying the SE position
    	if (x + i < NUM_ROWS && y + i < NUM_COLS && locationsAtEpoch[x + i][y + i] != 0) {
    		result.add(locationsAtEpoch[x + i][y + i]);
    	}

    	// verifying the NE position
    	if (x - i >= 0 && y + i < NUM_COLS && locationsAtEpoch[x - i][y + i] != 0) {
    		result.add(locationsAtEpoch[x - i][y + i]);
    	}

    	// verifying the SW position
    	if (x + i < NUM_ROWS && y - i >= 0 && locationsAtEpoch[x + i][y - i] != 0) {
    		result.add(locationsAtEpoch[x + i][y - i]);
    	}

    	// verifying the NW position
    	if (x - i >= 0 && y - i >= 0 && locationsAtEpoch[x - i][y - i] != 0) {
    		result.add(locationsAtEpoch[x - i][y - i]);
    	}

    }

}
