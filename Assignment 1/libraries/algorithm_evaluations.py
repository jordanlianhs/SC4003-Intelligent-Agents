import os
import numpy as np
import matplotlib.pyplot as plt

from libraries.value_iteration import value_iteration
from libraries.policy_iteration import policy_iteration, policy_iteration_modified
from libraries.utilities import make_directory, visualize_policy_subplot, plot_utilities

def Part1_VI_different_c_values(grid, rewards, actions, transition_probs, gamma, c_values, results_dir):
    """
    Analyzes how different values of the precision parameter c affect Value Iteration.
    
    Args:
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        c_values: list of c values to test
        results_dir: directory to save results
    """
    vi_records_list = []
    threshold_list = []

    # Create a figure for VI subplots
    fig_vi, axs_vi = plt.subplots(2, 2, figsize=(14, 12))
    axs_vi = axs_vi.flatten()

    for i, c in enumerate(c_values):
        epsilon = c * max(rewards.values())
        threshold = epsilon * (1 - gamma) / gamma
        threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))  
        threshold_list.append(threshold)
        print(f"Running Value Iteration with c={c} (threshold={threshold})...")
        
        # Run Value Iteration
        vi_utilities, vi_policy, vi_records = value_iteration(
            grid, rewards, actions, transition_probs, gamma, threshold)
        
        vi_records_list.append(vi_records)
        
        # Plot policy in subplot
        visualize_policy_subplot(grid, vi_policy, vi_utilities, axs_vi[i], f"c={c}, threshold={threshold}")
    
    plt.tight_layout()
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                         results_dir, 'VI_Optimal_Policy_at_Different_c_values.png'))
    plt.close()
    
    # Plot utility values for each c value
    fig_util, axs_util = plt.subplots(2, 2, figsize=(14, 12))
    axs_util = axs_util.flatten()
    
    for i, (c, records) in enumerate(zip(c_values, vi_records_list)):
        for pos, utilities in records.items():
            axs_util[i].plot(utilities, label=f'{pos}')
        axs_util[i].set_xlabel('Number of Iterations')
        axs_util[i].set_ylabel('Utility Value')
        axs_util[i].set_title(f'Value Iteration: c = {c}, threshold = {threshold_list[i]}')
        axs_util[i].grid(True)
      
    plt.tight_layout()
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                         results_dir, 'VI_Utility_Plots_at_Different_c_values.png'))
    plt.close()

def Part1_PI_different_c_values(grid, rewards, actions, transition_probs, gamma, c_values, results_dir):
    """
    Analyzes how different values of the precision parameter c affect Policy Iteration.
    
    Args:
        grid: the environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        c_values: list of c values to test
        results_dir: directory to save results
    """
    pi_eval_records_list = []
    pi_improv_records_list = []
    threshold_list = []
    
    # Create a figure for PI subplots
    fig_pi, axs_pi = plt.subplots(2, 2, figsize=(14, 12))
    axs_pi = axs_pi.flatten()
    
    for i, c in enumerate(c_values):
        epsilon = c * max(rewards.values())
        threshold = epsilon * (1 - gamma) / gamma
        threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))
        threshold_list.append(threshold)
        print(f"Running Policy Iteration with c={c} (threshold={threshold})...")
        
        # Run Policy Iteration
        pi_utilities, pi_policy, pi_eval_records, pi_improv_records = policy_iteration(
            grid, rewards, actions, transition_probs, gamma, threshold)
        
        pi_eval_records_list.append(pi_eval_records)
        pi_improv_records_list.append(pi_improv_records)
               
        # Plot policy in subplot
        visualize_policy_subplot(grid, pi_policy, pi_utilities, axs_pi[i], f"c={c}, threshold={threshold}")
    
    plt.tight_layout()
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                         results_dir, 'PI_Optimal_Policy_at_Different_c_values.png'))
    plt.close()
    
    # Plot evaluation utilities for each c value
    fig_eval, axs_eval = plt.subplots(2, 2, figsize=(14, 12))
    axs_eval = axs_eval.flatten()
    
    for i, (c, records) in enumerate(zip(c_values, pi_eval_records_list)):
        for pos, utilities in records.items():
            axs_eval[i].plot(utilities, label=f'{pos}')
        axs_eval[i].set_xlabel('Number of Iterations')
        axs_eval[i].set_ylabel('Utility Value')
        axs_eval[i].set_title(f'Policy Iteration Evaluation: c = {c}, threshold = {threshold_list[i]}')
        axs_eval[i].grid(True)

    
    plt.tight_layout()
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                         results_dir, 'PI_Evaluation_Utility_Plots_at_Different_c_values.png'))
    plt.close()
    
    # Plot improvement utilities for each c value
    fig_improv, axs_improv = plt.subplots(2, 2, figsize=(14, 12))
    axs_improv = axs_improv.flatten()
    
    for i, (c, records) in enumerate(zip(c_values, pi_improv_records_list)):
        for pos, utilities in records.items():
            axs_improv[i].plot(utilities, label=f'{pos}')
        axs_improv[i].set_xlabel('Number of Iterations')
        axs_improv[i].set_ylabel('Utility Value')
        axs_improv[i].set_title(f'Policy Iteration Improvement: c = {c}, threshold = {threshold_list[i]}')
        axs_improv[i].grid(True)
    
    
    plt.tight_layout()
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                         results_dir, 'PI_Improvement_Utility_Plots_at_Different_c_values.png'))
    plt.close()

def Part2_VI_different_c_values(complex_grid, rewards, actions, transition_probs, gamma, c_values, results_dir):
    """
    Analyzes how different values of the precision parameter c affect Value Iteration
    on a complex grid environment.
    
    Args:
        complex_grid: the complex environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        c_values: list of c values to test
        results_dir: directory to save results
    """
    vi_iterations = []
    for c in c_values:
        epsilon = c * max(rewards.values())
        threshold = epsilon * (1 - gamma) / gamma
        threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))  
        print(f"Running Value Iteration with c={c} (threshold={threshold})...")
        
        _, _, vi_records = value_iteration(complex_grid, rewards, actions, transition_probs, gamma, threshold)

        iterations = len(list(vi_records.values())[0])
        vi_iterations.append(iterations)
        
        # Plot utilities for this run
        plot_utilities(vi_records, results_dir, f"VI Utilities, c={c}, threshold={threshold}", f"VI_c_{c}")
    
    # Plot iterations vs c values
    plt.figure(figsize=(10, 6))
    plt.plot(c_values, vi_iterations, 'bo-')
    plt.xscale('log')
    plt.xlabel('c value (log scale)')
    plt.ylabel('Number of Iterations')
    plt.title('Value Iteration: Iterations Required vs c Value')
    plt.grid(True)
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                             results_dir, 'VI_iterations_vs_c.png'))
    
def Part2_PI_PIModified_different_k_values(complex_grid, rewards, actions, transition_probs, gamma, c, k_values, results_dir):
    """
    Compares Regular Policy Iteration with Modified Policy Iteration using different k values
    on a complex grid environment.
    
    Args:
        complex_grid: the complex environment grid
        rewards: dictionary mapping cell types to rewards
        actions: list of possible actions
        transition_probs: dictionary with intended and side action probabilities
        gamma: discount factor
        c: precision parameter for regular Policy Iteration
        k_values: list of k values to test for Modified Policy Iteration
        results_dir: directory to save results
    """
    epsilon = c * max(rewards.values())
    threshold = epsilon * (1 - gamma) / gamma
    threshold = round(threshold, -int(np.floor(np.log10(abs(threshold)))))
    print(f"Selected c value: {c} with threshold: {threshold}")

    print(f"Running Regular Policy Iteration with c = {c}...")
    pi_utilities, pi_policy, pi_eval_records, pi_improv_records = policy_iteration(
        complex_grid, rewards, actions, transition_probs, gamma, threshold)

    # Calculate iterations for evaluation and improvement
    pi_eval_iterations = len(list(pi_eval_records.values())[0])
    pi_improv_iterations = len(list(pi_improv_records.values())[0])
    
    # Plot utilities for regular policy iteration
    plot_utilities(pi_eval_records, results_dir,
                    f"Regular_PI_Evaluation, c={c}, threshold={threshold}", "Regular_PI_Evaluation")
    plot_utilities(pi_improv_records, results_dir,
                    f"Regular_PI_Improvement, c={c}, threshold={threshold}", "Regular_PI_Improvement")

    print()
    print(f"Varying k values for Modified Policy Iteration: {k_values}")
    mpi_eval_iterations_list = []
    mpi_improv_iterations_list = []

    for k in k_values:
        print(f"Running Modified Policy Iteration with k = {k}...")
        _, _, mpi_eval_records, mpi_improv_records = policy_iteration_modified(
            complex_grid, rewards, actions, transition_probs, gamma, k)
        
        mpi_eval_iterations = len(list(mpi_eval_records.values())[0])
        mpi_improv_iterations = len(list(mpi_improv_records.values())[0])
        
        mpi_eval_iterations_list.append(mpi_eval_iterations)
        mpi_improv_iterations_list.append(mpi_improv_iterations)
        
        # Plot utilities for each k value
        plot_utilities(mpi_eval_records, results_dir, f"Modified_PI_k_{k}_Evaluation", f"Modified_PI_k_{k}_Evaluation")
        plot_utilities(mpi_improv_records, results_dir, f"Modified_PI_k_{k}_Improvement", f"Modified_PI_k_{k}_Improvement")

    # Plot comparison metrics
    plt.figure(figsize=(10, 5))
    
    # Plot 1: Evaluation iterations 
    plt.subplot(2, 1, 1)
    plt.plot(k_values, mpi_eval_iterations_list, 'go-', label='Modified PI Eval Iterations')
    plt.axhline(y=pi_eval_iterations, color='r', linestyle='-', label='Regular PI Eval Iterations')
    plt.xlabel('k value')
    plt.ylabel('Evaluation Iterations')
    plt.legend()
    plt.title('Evaluation Iterations Comparison')
    plt.grid(True)
    
    # Plot 2: Policy improvement steps
    plt.subplot(2, 1, 2)
    plt.plot(k_values, mpi_improv_iterations_list, 'mo-', label='Modified PI Policy Improvements')
    plt.axhline(y=pi_improv_iterations, color='r', linestyle='-', label='Regular PI Policy Improvements')
    plt.xlabel('k value')
    plt.ylabel('Policy Improvement Steps')
    plt.legend()
    plt.title('Policy Improvement Steps Comparison') 
    plt.grid(True)
    
    plt.tight_layout()
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                            results_dir, 'PI_vs_Modified_PI_Comparison.png'))

