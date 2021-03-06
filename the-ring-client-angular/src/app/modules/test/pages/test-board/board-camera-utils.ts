const X_WIDTH = '-99%';
const Y_WIDTH = '-50%';

export const TOP_LEFT = 'top-left';
export const TOP_RIGHT = 'top-right';
export const BOTTOM_LEFT = 'bottom-left';
export const BOTTOM_RIGHT = 'bottom-right';

export const translateStyles = {
  'top-left': {
    HORIZONTAL: {
      x: X_WIDTH,
      y: '0',
      destination: 'top-right'
    },
    VERTICAL: {
      x: '0',
      y: Y_WIDTH,
      destination: 'bottom-left'
    }
  },
  'top-right': {
    HORIZONTAL: {
      x: '0',
      y: '0',
      destination: 'top-left'
    },
    VERTICAL: {
      x: X_WIDTH,
      y: Y_WIDTH,
      destination: 'bottom-right'
    }
  },
  'bottom-left': {
    HORIZONTAL: {
      x: X_WIDTH,
      y: Y_WIDTH,
      destination: 'bottom-right'
    },
    VERTICAL: {
      x: '0',
      y: '0',
      destination: 'top-left'
    }
  },
  'bottom-right': {
    HORIZONTAL: {
      x: '0',
      y: Y_WIDTH,
      destination: 'bottom-left'
    },
    VERTICAL: {
      x: X_WIDTH,
      y: '0',
      destination: 'top-right'
    }
  }
}

export const availableCameraMoves = {
  'top-left': ['right', 'bottom'],
  'top-right': ['left', 'bottom'],
  'bottom-left': ['top', 'right'],
  'bottom-right': ['top', 'left']
}

