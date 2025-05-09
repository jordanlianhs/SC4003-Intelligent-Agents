import numpy as np

from libraries.action import move, side_actions


def policy_evaluation(policy, utilities, grid, rewards, actions, transition_probs, gamma, policy_eval_records, policy_improv_records, threshold):
    """
    Evaluates a policy by calculating the utility of each state under that policy.
    
    Args:
        policy: current policy mapping states to actions
        utilities: current utility values for each state
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        policy_eval_records: dictionary to track evaluation utilities over iterations
        policy_improv_records: dictionary to track improvement utilities over iterations
        threshold: convergence threshold
        
    Returns:
        utilities: final utility values for each state
        eval_iterations: number of policy evaluation iterations taken to converge
    """
    height, width = len(grid), len(grid[0])
    # Record policy improvement utilities for tracked positions
    for pos in policy_improv_records:
        if 0 <= pos[0] < height and 0 <= pos[1] < width:
            policy_improv_records[pos].append(utilities[pos[0]][pos[1]])
    eval_iterations = 0
    while True:
        eval_iterations += 1
        delta = 0
        new_utilities = np.zeros((height, width))
        # Record policy evaluation utilities for tracked positions
        for pos in policy_eval_records:
            if 0 <= pos[0] < height and 0 <= pos[1] < width:
                policy_eval_records[pos].append(utilities[pos[0]][pos[1]])
        for row in range(height):
            for col in range(width):
                if grid[row][col] == 'WALL':
                    continue
                # Default to UP if no policy defined
                action = policy.get((row, col), 'UP')
                # Calculate expected utility
                expected_utility = 0
                # For intended action
                new_row, new_col = move(row, col, action, grid)
                expected_utility += transition_probs['intended'] * utilities[new_row][new_col]
                # For side actions
                for side_action in side_actions(action):
                    new_row, new_col = move(row, col, side_action, grid)
                    expected_utility += transition_probs['side'] * utilities[new_row][new_col]
                new_utilities[row][col] = rewards[grid[row][col]] + gamma * expected_utility
                delta = max(delta, abs(new_utilities[row][col] - utilities[row][col]))
        utilities = new_utilities
        # Check for convergence
        if delta < threshold:
            break
    return utilities, eval_iterations


def policy_iteration(grid, rewards, actions, transition_probs, gamma, threshold):
    """
    Implements the Policy Iteration algorithm for finding optimal policy.
    
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
        policy_eval_records: dictionary tracking policy evaluation utilities
        policy_improv_records: dictionary tracking policy improvement utilities
    """
    height, width = len(grid), len(grid[0])
    utilities = np.zeros((height, width))
    # Initialize policy
    policy = {}
    for row in range(height):
        for col in range(width):
            if grid[row][col] != 'WALL':
                policy[(row, col)] = 'UP'  # Fixed initial policy
    # Track utility values for all non-wall positions
    positions_to_track = []
    for row in range(height):
        for col in range(width):
            if grid[row][col] != 'WALL':
                positions_to_track.append((row, col))
    # Modified utility records to track both evaluation iterations and policy changes
    policy_eval_records  = {position: [] for position in positions_to_track}
    policy_improv_records = {position: [] for position in positions_to_track}
    policy_eval = 0
    policy_improv = 0
    while True:  # Policy is stable for a single iteration
        # Policy evaluation
        utilities, eval_iterations = policy_evaluation(policy, utilities, grid, rewards, actions, transition_probs, gamma, policy_eval_records, policy_improv_records, threshold)
        policy_eval += eval_iterations
        # Policy improvement
        policy_stable = True
        policy_improv += 1
        for row in range(height):
            for col in range(width):
                if grid[row][col] == 'WALL':
                    continue
                old_action = policy.get((row, col))
                # Calculate expected utilities for all actions
                action_utilities = []
                for action in actions:
                    expected_utility = 0
                    # For intended action
                    new_row, new_col = move(row, col, action, grid)
                    expected_utility += transition_probs['intended'] * utilities[new_row][new_col]
                    # For side actions
                    for side_action in side_actions(action):
                        new_row, new_col = move(row, col, side_action, grid)
                        expected_utility += transition_probs['side'] * utilities[new_row][new_col]
                    action_utilities.append(expected_utility)
                # Choose the best action
                best_action_idx = np.argmax(action_utilities)
                policy[(row, col)] = actions[best_action_idx]
                # Check if policy changed
                if old_action != policy[(row, col)]:
                    policy_stable = False
        # Policy is stable for a single iteration , we've found the optimal policy
        if policy_stable:
            break
    print(f"Policy Iteration converged after {policy_eval} policy evaluation iterations and {policy_improv} policy improvement iterations")
    return utilities, policy, policy_eval_records, policy_improv_records


def policy_evaluation_modified(policy, utilities, grid, rewards, actions, transition_probs, gamma, policy_eval_records, policy_improv_records, k_iterations):
    """
    Evaluates a policy by calculating the utility of each state under that policy
    for a fixed number of iterations instead of until convergence.
    
    Args:
        policy: current policy mapping states to actions
        utilities: current utility values for each state
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        policy_eval_records: dictionary to track evaluation utilities over iterations
        policy_improv_records: dictionary to track improvement utilities over iterations
        k_iterations: number of iterations to perform
        
    Returns:
        utilities: final utility values for each state
        eval_iterations: number of policy evaluation iterations taken
    """
    height, width = len(grid), len(grid[0])
    # Record policy improvement utilities for tracked positions
    for pos in policy_improv_records:
        if 0 <= pos[0] < height and 0 <= pos[1] < width:
            policy_improv_records[pos].append(utilities[pos[0]][pos[1]])
    eval_iterations = 0
    for _ in range(k_iterations):
        eval_iterations += 1
        new_utilities = np.zeros((height, width))
        # Record policy evaluation utilities for tracked positions
        for pos in policy_eval_records:
            if 0 <= pos[0] < height and 0 <= pos[1] < width:
                policy_eval_records[pos].append(utilities[pos[0]][pos[1]])
        for row in range(height):
            for col in range(width):
                if grid[row][col] == 'WALL':
                    continue
                # Default to UP if no policy defined
                action = policy.get((row, col), 'UP')
                # Calculate expected utility
                expected_utility = 0
                # For intended action
                new_row, new_col = move(row, col, action, grid)
                expected_utility += transition_probs['intended'] * utilities[new_row][new_col]
                # For side actions
                for side_action in side_actions(action):
                    new_row, new_col = move(row, col, side_action, grid)
                    expected_utility += transition_probs['side'] * utilities[new_row][new_col]
                new_utilities[row][col] = rewards[grid[row][col]] + gamma * expected_utility
        utilities = new_utilities
    return utilities, eval_iterations


def policy_iteration_modified(grid, rewards, actions, transition_probs, gamma, k_iterations):
    """
    Implements the Modified Policy Iteration algorithm for finding optimal policy,
    using a fixed number of evaluation iterations instead of evaluating until convergence.
    
    Args:
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        k_iterations: number of iterations for policy evaluation
        
    Returns:
        utilities: final utility values for each state
        policy: optimal policy for each state
        policy_eval_records: dictionary tracking policy evaluation utilities
        policy_improv_records: dictionary tracking policy improvement utilities
    """
    height, width = len(grid), len(grid[0])
    utilities = np.zeros((height, width))
    # Initialize policy
    policy = {}
    for row in range(height):
        for col in range(width):
            if grid[row][col] != 'WALL':
                policy[(row, col)] = 'UP'  # Fixed initial policy
    # Track utility values for all non-wall positions
    positions_to_track = []
    for row in range(height):
        for col in range(width):
            if grid[row][col] != 'WALL':
                positions_to_track.append((row, col))
    # Modified utility records to track both evaluation iterations and policy changes
    policy_eval_records  = {position: [] for position in positions_to_track}
    policy_improv_records = {position: [] for position in positions_to_track}
    policy_eval = 0
    policy_improv = 0  
    while True:  # Policy is stable for a single iteration
        # Policy evaluation with fixed number of iterations
        utilities, eval_iterations = policy_evaluation_modified(policy, utilities, grid, rewards, actions, transition_probs, gamma, policy_eval_records, policy_improv_records, k_iterations)
        policy_eval += eval_iterations
        # Policy improvement
        policy_stable = True
        policy_improv += 1  
        for row in range(height):
            for col in range(width):
                if grid[row][col] == 'WALL':
                    continue
                old_action = policy.get((row, col))
                # Calculate expected utilities for all actions
                action_utilities = []
                for action in actions:
                    expected_utility = 0
                    # For intended action
                    new_row, new_col = move(row, col, action, grid)
                    expected_utility += transition_probs['intended'] * utilities[new_row][new_col]
                    # For side actions
                    for side_action in side_actions(action):
                        new_row, new_col = move(row, col, side_action, grid)
                        expected_utility += transition_probs['side'] * utilities[new_row][new_col]
                    action_utilities.append(expected_utility)
                # Choose the best action
                best_action_idx = np.argmax(action_utilities)
                policy[(row, col)] = actions[best_action_idx]
                # Check if policy changed
                if old_action != policy[(row, col)]:
                    policy_stable = False
        # Policy is stable for a single iteration, we've found the optimal policy
        if policy_stable:
            break
    print(f"Modified Policy Iteration converged after {policy_eval} policy evaluation iterations and {policy_improv} policy improvement iterations")
    return utilities, policy, policy_eval_records, policy_improv_records
