import numpy as np

from libraries.value_iteration import value_iteration
from libraries.policy_iteration import policy_iteration, policy_iteration_modified
from libraries.utilities import plot_utilities, visualize_policy, save_utilities, visualize_grid, visualize_policy_subplot, make_directory
from libraries.algorithm_evaluations import Part1_VI_different_c_values, Part1_PI_different_c_values, Part2_VI_different_c_values, Part2_PI_PIModified_different_k_values

PART1_C_VALUE=1
PART2_C_VALUE=1
PART2_K_VALUE=50

def main():
    print("--------------------------------------------------------------------------")
    print('''PART 1:''')
    print("--------------------------------------------------------------------------\n")
    # Create the part 1 results directory if it doesn't exist
    results_dir = 'part_1_results'
    
    # Define the grid environment
    # Use 'G' for green cells, 'B' for brown cells, 'WHITE' for white cells, 'WALL' for walls
    grid = [
        ['G', 'WALL', 'G', 'WHITE', 'WHITE', 'G'],
        ['WHITE', 'B', 'WHITE', 'G', 'WALL', 'B'],
        ['WHITE', 'WHITE', 'B', 'WHITE', 'G', 'WHITE'],
        ['WHITE', 'WHITE', 'WHITE', 'B', 'WHITE', 'G'],
        ['WHITE', 'WALL', 'WALL', 'WALL', 'B', 'WHITE'],
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE']
    ]
    visualize_grid(grid, 'part_1_results', 'Grid Environment')

    # Define rewards
    # 'G' green cells reward 1, 'B' brown cells reward -1, 'WHITE' white cells reward -0.05, 'WALL' walls reward 0
    rewards = {'G': 1, 'B': -1, 'WHITE': -0.05, 'WALL': 0}

    # Define actions
    actions = ['UP', 'DOWN', 'LEFT', 'RIGHT']

    # Define transition probabilities
    transition_probs = {'intended': 0.8, 'side': 0.1}

    # Define discount factor and convergence precision
    # γ
    gamma = 0.99
    # Precision parameter for convergence, try different c values
    c_values = [50, 10, 1, 0.1]
    
    # For Value Iteration
    print("Experiment 1: Value Iteration with different values of c")
    print("--------------------------------------------------------")
    # COMMENT THIS LINE BELOW OUT IF YOU DON'T WANT TO RUN THE CROSS VALIDATION EXPERIMENT
    Part1_VI_different_c_values(grid, rewards, actions, transition_probs, gamma, c_values, results_dir+'/exp1')

    # Based on the results, we can see that 
    # When c is too large, the threshold is too small and the algorithm converges too quickly
    # When c is too small, the threshold is too large and the algorithm takes too long to converge
    # The optimal value of c is around 1, which provides a good balance between convergence rate and computational efficiency

    print("\nValue Iteration with c=1")
    print("--------------------------------------------------------")
    c = PART1_C_VALUE
    # ε = c * Rmax
    epsilon = c * max(rewards.values())
    # threshold = ε * (1 − γ)/γ
    threshold = epsilon * (1 - gamma) / gamma
    threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))  
    print(f"Selected c value: {c} with threshold: {threshold}")
    
    # Generate the final results with c=1
    print("Running Value Iteration with c=1 ...")
    vi_utilities, vi_policy, vi_records = value_iteration(
        grid, rewards, actions, transition_probs, gamma, threshold)
    
    # Plot utilities over iterations to png
    print("Plotting utility values over iterations...")
    plot_utilities(vi_records, results_dir, f"VI Utility against Iteration Plot, c= {c}, threshold= {threshold}",
                   "VI Utility against Iteration Plot")
    
    # Visualize the policies on the grid
    print("Visualizing policies on grid...")
    visualize_policy(grid, vi_policy, vi_utilities, results_dir, f"VI Optimal Policy, c= {c}, threshold= {threshold}",
                     "VI Optimal Policy")
    
    # Save utility values to text
    print("Saving utility values to text file...")
    save_utilities(vi_utilities, results_dir, "VI Utilities")
    print()

    
    # For Policy Iteration
    print("Experiment 2: Policy Iteration with different values of c")
    print("--------------------------------------------------------")
    # COMMENT THIS LINE BELOW OUT IF YOU DON'T WANT TO RUN THE CROSS VALIDATION EXPERIMENT
    Part1_PI_different_c_values(grid, rewards, actions, transition_probs, gamma, c_values, results_dir+'/exp2')

    # Based on the results, we can see that 
    # When c is too large, the threshold is too small and the algorithm converges too quickly
    # When c is too small, the threshold is too large and the algorithm takes too long to converge
    # The optimal value of c is around 1, which provides a good balance between convergence rate and computational efficiency

    # Policy Iteration with c=1
    print("\nPolicy Iteration with c=1")
    print("--------------------------------------------------------")
    c = PART1_C_VALUE
    # ε = c * Rmax
    epsilon = c * max(rewards.values())
    # threshold = ε * (1 − γ)/γ
    threshold = epsilon * (1 - gamma) / gamma
    threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))  
    print(f"Selected c value: {c} with threshold: {threshold}")
    
    # Generate the final results with c=1
    print(f"Running Policy Iteration with c={c}...")
    pi_utilities, pi_policy, pi_eval_records, pi_improv_records = policy_iteration(
        grid, rewards, actions, transition_probs, gamma, threshold)
    
    # Plot utilities over iterations to png
    print("Plotting utility values over iterations...")
    plot_utilities(pi_eval_records, results_dir, f"PI Utility against PolicyEvalIteration Plot, c= {c}, threshold= {threshold}",
                   "PI Utility against PolicyEvalIteration Plot")
    plot_utilities(pi_improv_records, results_dir, f"PI Utility against PolicyImprovIteration Plot, c= {c}, threshold= {threshold}",
                   "PI Utility against PolicyImprovIteration Plot")
    
    # Visualize the policies on the grid
    print("Visualizing policies on grid...")
    visualize_policy(grid, pi_policy, pi_utilities, results_dir, f"PI Optimal Policy, c= {c}, threshold= {threshold}",
                     "PI Optimal Policy")
    
    # Save utility values to text
    print("Saving utility values to text file...")
    save_utilities(pi_utilities, results_dir, "PI Utilities")
    print()
    
    # Validate the policies to see if they are the same
    print("Comparing policies")
    print("--------------------------------------------------------")
    # If they are not the same, one/both of the algorithm has a bug or one/both of them have not converged
    match_count = 0
    total = 0
    for row in range(len(grid)):
        for col in range(len(grid[0])):
            if grid[row][col] != 'WALL':
                total += 1
                if vi_policy.get((row, col)) == pi_policy.get((row, col)):
                    match_count += 1

    print(
        f"Policy agreement: {match_count}/{total} states ({match_count/total*100:.2f}%)\n\n")

    
    print("--------------------------------------------------------------------------")
    print('''PART 2:''')
    print("--------------------------------------------------------------------------\n")
    results_dir = 'part_2_results'

    # Define the grid environment
    # Use 'G' for green cells, 'B' for brown cells, 'WHITE' for white cells, 'WALL' for walls
    complex_grid = [
        ['WHITE', 'WALL', 'G', 'WALL', 'G', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'G', 'WHITE'],
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WALL', 'G', 'WHITE', 'WHITE', 'WHITE', 'WHITE'],
        ['G', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'WHITE', 'G', 'WHITE', 'WHITE', 'WHITE', 'B'], 
        ['WHITE', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'B', 'WALL', 'WHITE', 'WHITE', 'B', 'WALL'], 
        ['WHITE', 'G', 'WHITE', 'WALL', 'B', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'G', 'WHITE', 'WHITE'], 
        ['WALL', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'G', 'WHITE', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'WHITE'], 
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'B', 'WHITE', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'WHITE'], 
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'B', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'WHITE', 'B'], 
        ['WHITE', 'WALL', 'WHITE', 'WHITE', 'B', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WALL', 'WALL', 'WHITE'], 
        ['WHITE', 'WHITE', 'WHITE', 'WALL', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'G', 'WHITE', 'WHITE'], 
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'B', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'G', 'WHITE'], 
        ['WHITE', 'WHITE', 'WHITE', 'WHITE', 'B', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE', 'WHITE']
        ]
    visualize_grid(complex_grid, 'part_2_results', 'Complex Grid Environment')

    # Define rewards
    # 'G' green cells reward 1, 'B' brown cells reward -1, 'WHITE' white cells reward -0.05, 'WALL' walls reward 0
    rewards = {'G': 1, 'B': -1, 'WHITE': -0.05, 'WALL': 0}

    # Define actions
    actions = ['UP', 'DOWN', 'LEFT', 'RIGHT']

    # Define transition probabilities
    transition_probs = {'intended': 0.8, 'side': 0.1}

    # Define discount factor and convergence precision
    # γ
    gamma = 0.99
    # Precision parameter for convergence
    c_values = [50, 10, 1, 0.1]
    
    # For Value Iteration
    print("Experiment 1: Value Iteration with different values of c")
    print("--------------------------------------------------------")
    # COMMENT THIS LINE BELOW OUT IF YOU DON'T WANT TO RUN THE CROSS VALIDATION EXPERIMENT
    Part2_VI_different_c_values(complex_grid, rewards, actions, transition_probs, gamma, c_values, results_dir+'/exp1') 
        
    # Based on the results, we can see that 
    # When c is too large, the threshold is too small and the algorithm converges too quickly
    # When c is too small, the threshold is too large and the algorithm takes too long to converge
    # The optimal value of c is around 1, which provides a good balance between convergence rate and computational efficiency

    print("\nValue Iteration with c=1")
    print("--------------------------------------------------------")
    # Run Value Iteration with c=1
    c = PART2_C_VALUE
    epsilon = c * max(rewards.values()) # ε = c * Rmax
    threshold = epsilon * (1 - gamma) / gamma # threshold = ε * (1 − γ)/γ
    threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))  
    print(f"Selected c value: {c} with threshold: {threshold}")

    print(f"Running Value Iteration with c={c}...")
    vi_utilities, vi_policy, vi_records = value_iteration(
        complex_grid, rewards, actions, transition_probs, gamma, threshold)

    # Plot utilities over iterations to png
    print("Plotting utility values over iterations...")
    plot_utilities(vi_records, results_dir, f"VI Utility against Iteration Plot, c= {c}, threshold= {threshold}",
                    "VI Utility against Iteration Plot")

    # Visualize the policies on the grid
    print("Visualizing policies on grid...")
    visualize_policy(complex_grid, vi_policy,  vi_utilities, results_dir, f"VI Optimal Policy, c= {c}, threshold= {threshold}",
                     "VI Optimal Policy")

    # Save utility values to text
    print("Saving utility values to text file...")
    save_utilities(vi_utilities, results_dir, "VI Utilities")
    print()


    # For Policy Iteration
    print("Experiment 2: Regular vs Modified Policy Iteration algorithm")
    print("--------------------------------------------------------")
    # Set c=1 for Regular Policy Iteration
    c = PART2_C_VALUE
    # Try different k values for Modified Policy Iteration
    k_values = [5, 10, 20, 50, 100]
    # COMMENT THIS LINE BELOW OUT IF YOU DON'T WANT TO RUN THE CROSS VALIDATION EXPERIMENT
    Part2_PI_PIModified_different_k_values(complex_grid, rewards, actions, transition_probs, gamma, c, k_values, results_dir+'/exp2')

    # We note that the modified policy iteration algorithm converges faster than the regular policy iteration algorithm
    # This is because the modified policy iteration algorithm evaluates the policy for a fixed number of iterations (k)
    # This allows the algorithm to converge faster than the regular policy iteration algorithm
    # For small state spaces, as the equations are linear, policy evaluation using exact solution methods is often the most efficient approach
    # For large state spaces, iterative methods such as modified policy iteration are more efficient,
    # it is not necessary to do exact policy evaluation at each step
    # The choice of k value depends on the size of the state space and the desired convergence rate

    # Run modified policy iteration with k=50 which is a good balance between convergence rate and computational efficiency
    print("\nModified Policy Iteration with k=50...")
    print("--------------------------------------------------------")
    k = PART2_K_VALUE
    print(f"Running Modified Policy Iteration with k={k}...")
    mpi_utilities, mpi_policy, mpi_eval_records, mpi_improv_records = policy_iteration_modified(
        complex_grid, rewards, actions, transition_probs, gamma, k)
    
    # Plot utilities over iterations to png
    print("Plotting utility values over iterations...")
    plot_utilities(mpi_eval_records, results_dir, f"Modified_PI_k_{k}_Evaluation", f"Modified_PI_k_{k}_Evaluation")
    plot_utilities(mpi_improv_records, results_dir, f"Modified_PI_k_{k}_Improvement", f"Modified_PI_k_{k}_Improvement")

    # Visualize the policies on the grid
    print("Visualizing policies on grid...")
    visualize_policy(complex_grid, mpi_policy, mpi_utilities, results_dir, 
                    f"MPI_k_{k}_Optimal_Policy", f"MPI_k_{k}_Optimal_Policy")

    # Save utility values to text
    print("Saving utility values to text file...")
    save_utilities(mpi_utilities, results_dir, f"MPI_k_{k}_Utilities")
    print()

    # Compare the policies
    print("Comparing policies")
    print("--------------------------------------------------------")
    match_count = 0
    total = 0
    for row in range(len(complex_grid)):
        for col in range(len(complex_grid[0])):
            if complex_grid[row][col] != 'WALL':
                total += 1
                if vi_policy.get((row, col)) == mpi_policy.get((row, col)):
                    match_count += 1
    print(
        f"Policy agreement: {match_count}/{total} states ({match_count/total*100:.2f}%)\n\n")


if __name__ == "__main__":#
    main()
