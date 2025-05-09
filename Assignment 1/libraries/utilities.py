import os
import matplotlib.pyplot as plt
import numpy as np

def make_directory(results_dir):
    '''
    Creates a results directory to store output files if it doesn't already exist.
    
    Args:
        results_dir: path to the directory to create
    '''
    os.makedirs(os.path.join(os.path.dirname(
        os.path.abspath(__file__)), results_dir), exist_ok=True)

def plot_utilities(utility_records, results_dir, title, filename):
    """
    Plots the utility values over iterations for tracked positions.
    
    Args:
        utility_records: dictionary mapping positions to lists of utility values
        results_dir: directory to save the plot
        title: title for the plot
        filename: filename for the saved plot
    """
    # adjust plot size based on the grid size, number of cells to track
    plt.figure(figsize=(len(utility_records) // 2, len(utility_records) // 4))

    for pos, utilities in utility_records.items():
        plt.plot(utilities, label=f'{pos}')

    plt.xlabel('Number of Iterations')
    plt.ylabel('Utility Value')
    plt.title(f'Utility Values - {title}')
    plt.legend(loc='center left', bbox_to_anchor=(1, 0.5))
    plt.grid(True)

    # Save the plot to the results directory
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                results_dir) + f'/{filename.replace(" ", "_")}.png')
    plt.close()

def visualize_policy_subplot(grid, policy, utilities, ax, title):
    """
    Visualizes the policy on the grid in a given subplot.
    
    Args:
        grid: the environment grid
        policy: policy mapping states to actions
        utilities: utility values for each state
        ax: matplotlib axis to plot on
        title: title for the subplot
    """
    height, width = len(grid), len(grid[0])

    # Define colors for grid cells
    colors = {
        'G': '#98FB98',  # Light green
        'B': '#FFA500',  # Orange
        'WHITE': 'white',
        'WALL': 'gray'
    }

    # Define arrow symbols for actions
    arrows = {
        'UP': '^',
        'DOWN': 'v',
        'LEFT': '<',
        'RIGHT': '>'
    }

    ax.axis('off')

    # Draw grid
    for row in range(height):
        for col in range(width):
            cell_type = grid[row][col]

            # Draw cell with color based on type
            rect = plt.Rectangle((col, height-1-row), 1, 1,
                               edgecolor='black',
                               facecolor=colors[cell_type])
            ax.add_patch(rect)

            # Draw action arrow if not a wall
            if cell_type != 'WALL':
                action = policy.get((row, col), None)
                if action:  # Draw arrow if action is defined
                    ax.plot(col+0.5, height-0.5-row,
                          marker=arrows[action], markersize=15, color='black')

    # Set grid dimensions
    ax.set_xlim(0, width)
    ax.set_ylim(0, height)
    ax.set_aspect('equal')
    ax.set_title(title)

    
def visualize_policy(grid, policy, utilities, results_dir, title, filename):
    """
    Visualizes the policy on the grid and saves the visualization.
    
    Args:
        grid: the environment grid
        policy: policy mapping states to actions
        utilities: utility values for each state
        results_dir: directory to save the visualization
        title: title for the visualization
        filename: filename for the saved visualization
    """
    height, width = len(grid), len(grid[0])

    # Define colors for grid cells
    colors = {
        'G': '#98FB98',  # Light green
        'B': '#FFA500',  # Orange
        'WHITE': 'white',
        'WALL': 'gray'
    }

    # Define arrow symbols for actions
    arrows = {
        'UP': '^',
        'DOWN': 'v',
        'LEFT': '<',
        'RIGHT': '>'
    }

    # adjust plot size based on grid size
    fig, ax = plt.subplots(figsize=(width+2, height+2))
    ax.axis('off')

    # Draw grid
    for row in range(height):
        for col in range(width):
            cell_type = grid[row][col]

            # Draw cell with color based on type
            rect = plt.Rectangle((col, height-1-row), 1, 1,
                                 edgecolor='black',
                                 facecolor=colors[cell_type])
            ax.add_patch(rect)

            # Draw action arrow if not a wall
            if cell_type != 'WALL':
                action = policy.get((row, col), None)
                if action:  # Draw arrow if action is defined
                    ax.plot(col+0.5, height-0.5-row,
                            marker=arrows[action], markersize=15, color='black')

            # Add coordinates to cell
            ax.text(col+0.1, height-0.1-row,
                    f'({row}, {col})', color='black', ha='left', va='top', fontsize=10)

            # Add utility value to cell
            utility_val = utilities[row, col]
            utility_val = 0 if utility_val == 0 else round(utility_val, 6)
            ax.text(col+0.5, height-0.8-row,
                    f"{utility_val}", ha='center', va='center', fontsize=10)

    # Set grid dimensions
    ax.set_xlim(0, width)
    ax.set_ylim(0, height)
    ax.set_aspect('equal')

    plt.title(title)
    # Save the plot to the results directory
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                results_dir) + f'/{filename.replace(" ", "_")}.png')
    plt.close()

def visualize_grid(grid, results_dir, title):
    """
    Visualizes the grid environment and saves the visualization.

    Args:
        grid: the environment grid
        results_dir: directory to save the visualization
        title: title for the visualization    
    """
    height, width = len(grid), len(grid[0])

    # Define colors for grid cells
    colors = {
        'G': '#98FB98',  # Light green
        'B': '#FFA500',  # Orange
        'WHITE': 'white',
        'WALL': 'gray'
    }

    # adjust plot size based on grid size
    fig, ax = plt.subplots(figsize=(width+2, height+2))
    ax.axis('off')

    # Draw grid
    for row in range(height):
        for col in range(width):
            cell_type = grid[row][col]

            # Draw cell with color based on type
            rect = plt.Rectangle((col, height-1-row), 1, 1,
                                 edgecolor='black',
                                 facecolor=colors[cell_type])
            ax.add_patch(rect)

            # Add coordinates to cell
            ax.text(col+0.1, height-0.1-row,
                    f'({row}, {col})', color='black', ha='left', va='top', fontsize=10)

    # Set grid dimensions
    ax.set_xlim(0, width)
    ax.set_ylim(0, height)
    ax.set_aspect('equal')

    plt.title(title)
    # Save the plot to the results directory
    make_directory(results_dir)
    plt.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                results_dir) + f'/{title.replace(" ", "_")}.png')
    plt.close()

def save_utilities(utilities, results_dir, filename):
    """
    Saves the utility values to a markdown table and a text file.

    Args:
        utilities: utility values for each state
        results_dir: directory to save the utility values
        filename: filename for the saved utility values
    """
    height, width = utilities.shape

    # Create markdown table
    make_directory(results_dir)
    with open(os.path.join(os.path.dirname(os.path.abspath(__file__)), results_dir) + f'/{filename.replace(" ", "_")}.md', 'w') as f:
        # Write title
        f.write('# UTILITY VALUE OF ALL STATES\n\n')

        # Write table header with column indices
        f.write('|   | ' + ' | '.join([str(i) for i in range(width)]) + ' |\n')
        f.write('|---|' + '---|' * width + '\n')

        # Write table rows with row indices
        for row in range(height):
            row_values = []
            for col in range(width):
                value = utilities[row, col]
                if value == 0:
                    row_values.append('0')
                else:
                    row_values.append(f'{value:.6f}')
            f.write(f'| {row} | ' + ' | '.join(row_values) + ' |\n')

    # Also save as text file
    np.savetxt(os.path.join(os.path.dirname(os.path.abspath(__file__)),
               results_dir) + f'/{filename.replace(" ", "_")}.txt', utilities, fmt='%.6f')
