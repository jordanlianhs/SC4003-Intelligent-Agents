import numpy as np


def create_complex_grid(height, width):
    """
    Creates a more complex grid environment with a specific height and width.
    Randomly generates green (+1), brown (-1), white (-0.05) cells, and walls.
    """
    # Create an empty grid filled with white cells
    grid = [['WHITE' for _ in range(width)] for _ in range(height)]

    # Set the seed for reproducibility
    np.random.seed(0)

    # Add walls (approximately 15% of the grid)
    num_walls = int(height * width * 0.15)
    for _ in range(num_walls):
        row, col = np.random.randint(0, height), np.random.randint(0, width)
        grid[row][col] = 'WALL'

    # Add green cells (approximately 10% of the grid)
    num_green = int(height * width * 0.1)
    for _ in range(num_green):
        row, col = np.random.randint(0, height), np.random.randint(0, width)
        if grid[row][col] == 'WHITE':  # Only change if it's a white cell
            grid[row][col] = 'G'

    # Add brown cells (approximately 10% of the grid)
    num_brown = int(height * width * 0.1)
    for _ in range(num_brown):
        row, col = np.random.randint(0, height), np.random.randint(0, width)
        if grid[row][col] == 'WHITE':  # Only change if it's a white cell
            grid[row][col] = 'B'

    return grid

print(create_complex_grid(12, 12))
