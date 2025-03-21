export const hashToNumber = (str: string) => {
    let hash = 2166136261; // FNV-1a 32-bit offset basis
    for (let i = 0; i < str.length; i++) {
        hash ^= str.charCodeAt(i);
        hash *= 16777619; // FNV prime
    }

    // Convert to an unsigned 32-bit integer
    hash >>>= 0; 

    // Normalize to a float between 0 and 1
    return hash / 2 ** 32;
}