import numpy as np

from libraries.action import move, side_actions

def value_iteration(grid, rewards, actions, transition_probs, gamma, threshold):
    """
    implements value iteration algorithm for finding optimal policy.
    
    Args:
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        threshold: convergence threshold
        
    Returns:
        utilities: final utility values for each state
        policy: optimal policy for each state
        utility_records: dictionary tracking utilities for specific positions over iterations
    """
    height, width = len(grid), len(grid[0])
    utilities = np.zeros((height, width))
    policy = {}
    # Track utility values for all non-wall positions
    positions_to_track = []
    for row in range(height):
        for col in range(width):
            if grid[row][col] != 'WALL':
                positions_to_track.append((row, col))
    utility_records = {pos: [] for pos in positions_to_track}
    iteration = 0
    while True:
        iteration += 1
        delta = 0
        new_utilities = np.zeros((height, width))
        # Record utilities for tracked positions
        for pos in utility_records:
            if 0 <= pos[0] < height and 0 <= pos[1] < width:
                utility_records[pos].append(utilities[pos[0]][pos[1]])
        for row in range(height):
            for col in range(width):
                if grid[row][col] == 'WALL':
                    continue
                # Calculate the expected utility for each action
                action_utilities = []
                for action in actions:
                    expected_utility = 0
                    # Calculate for intended action (probability 0.8)
                    new_row, new_col = move(row, col, action, grid)
                    expected_utility += transition_probs['intended'] * utilities[new_row][new_col]
                    # Calculate for side actions (probability 0.1 each)
                    for side_action in side_actions(action):
                        new_row, new_col = move(row, col, side_action, grid)
                        expected_utility += transition_probs['side'] * utilities[new_row][new_col]
                    action_utilities.append(expected_utility)
                # Choose the action that maximizes utility
                best_action_idx = np.argmax(action_utilities)
                new_utilities[row][col] = rewards[grid[row][col]] + gamma * action_utilities[best_action_idx]
                policy[(row, col)] = actions[best_action_idx]
                # Update delta for convergence check
                delta = max(delta, abs(new_utilities[row][col] - utilities[row][col]))
        # Update utilities for next iteration
        utilities = new_utilities
        # Check for convergence
        if delta < threshold:
            break
    print(f"Value Iteration converged after {iteration} iterations")
    return utilities, policy, utility_records