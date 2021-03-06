/*
 * Copyright (c) 2017, 2018, 2019 Adetunji Dahunsi.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tunjid.fingergestures;

public class BrightnessLookup {

    private static final int FLICKER_THRESHOLD = 24;

    public static int lookup(int query, boolean isByte) {
        int low = 0;
        int mid = 0;
        int high = table.length - 1;
        boolean increasing = true;

        // No direct mapping for bytes, just crawl for the lower values
        if (!isByte && query < FLICKER_THRESHOLD) return crawl(query, 0, false, true);

        while (low <= high) {
            mid = (low + high) / 2;
            int key = table[mid][isByte ? 0 : 1];
            int diff = query - key;
            increasing = diff > 0;

            if (Math.abs(diff) < 2) return crawl(query, mid, isByte, increasing);
            else if (increasing) low = mid + 1;
            else high = mid - 1;
        }

        return crawl(query, mid, isByte, increasing);
    }

    private static int crawl(int query, int index, boolean isByte, boolean increasing) {
        int i = index;
        int num = table.length;
        int key = isByte ? 0 : 1;
        int value = isByte ? 1 : 0;

        if (increasing) { for (; i < num; i++) if (table[i][key] >= query) return table[i][value]; }
        else for (; i >= 0; i--) if (table[i][key] <= query) return table[i][value];

        return 0;
    }

    private static final int[][] table = {
            {0, 0},
            {1, 6},
            {2, 11},
            {3, 15},
            {4, 19},
            {5, 22},
            {6, 24},
            {7, 27},
            {8, 29},
            {9, 31},
            {10, 33},
            {11, 34},
            {12, 36},
            {13, 38},
            {14, 39},
            {15, 41},
            {16, 42},
            {17, 43},
            {18, 45},
            {19, 46},
            {20, 47},
            {21, 49},
            {22, 50},
            {23, 51},
            {24, 52},
            {25, 53},
            {26, 54},
            {27, 55},
            {28, 56},
            {29, 57},
            {30, 57},
            {31, 57},
            {32, 57},
            {33, 60},
            {34, 60},
            {35, 61},
            {36, 62},
            {37, 62},
            {38, 63},
            {39, 63},
            {40, 64},
            {41, 64},
            {42, 65},
            {43, 65},
            {44, 66},
            {45, 66},
            {46, 67},
            {47, 67},
            {48, 68},
            {49, 68},
            {50, 69},
            {51, 69},
            {52, 70},
            {53, 70},
            {54, 70},
            {55, 71},
            {56, 71},
            {57, 71},
            {58, 72},
            {59, 72},
            {60, 72},
            {61, 73},
            {62, 73},
            {63, 73},
            {64, 74},
            {65, 74},
            {66, 74},
            {67, 75},
            {68, 75},
            {71, 76},
            {73, 76},
            {76, 77},
            {79, 78},
            {80, 78},
            {81, 78},
            {84, 79},
            {86, 80},
            {89, 80},
            {91, 81},
            {94, 81},
            {96, 82},
            {97, 82},
            {98, 82},
            {99, 82},
            {102, 83},
            {104, 83},
            {107, 84},
            {109, 84},
            {112, 85},
            {114, 85},
            {117, 85},
            {119, 86},
            {122, 87},
            {124, 87},
            {127, 87},
            {130, 88},
            {132, 88},
            {133, 88},
            {134, 88},
            {135, 88},
            {136, 88},
            {137, 88},
            {138, 89},
            {139, 89},
            {140, 89},
            {141, 89},
            {142, 89},
            {143, 89},
            {144, 89},
            {145, 90},
            {146, 90},
            {147, 90},
            {148, 90},
            {149, 90},
            {150, 90},
            {151, 90},
            {152, 91},
            {153, 91},
            {154, 91},
            {155, 91},
            {156, 91},
            {157, 91},
            {158, 91},
            {159, 91},
            {160, 91},
            {161, 91},
            {162, 92},
            {163, 92},
            {164, 92},
            {165, 92},
            {166, 92},
            {167, 92},
            {168, 92},
            {169, 92},
            {170, 92},
            {171, 93},
            {172, 93},
            {173, 93},
            {174, 93},
            {175, 93},
            {176, 93},
            {177, 93},
            {178, 93},
            {179, 93},
            {180, 94},
            {181, 94},
            {182, 94},
            {183, 94},
            {184, 94},
            {185, 94},
            {186, 94},
            {187, 94},
            {188, 94},
            {189, 94},
            {190, 95},
            {191, 95},
            {192, 95},
            {193, 95},
            {194, 95},
            {195, 95},
            {196, 95},
            {197, 95},
            {198, 95},
            {199, 95},
            {200, 96},
            {201, 96},
            {202, 96},
            {203, 96},
            {204, 96},
            {205, 96},
            {206, 96},
            {207, 96},
            {208, 96},
            {209, 96},
            {210, 96},
            {211, 96},
            {212, 97},
            {213, 97},
            {214, 97},
            {215, 97},
            {216, 97},
            {217, 97},
            {218, 97},
            {219, 97},
            {220, 97},
            {221, 97},
            {222, 97},
            {223, 98},
            {224, 98},
            {225, 98},
            {226, 98},
            {227, 98},
            {228, 98},
            {229, 98},
            {230, 98},
            {231, 98},
            {232, 98},
            {233, 98},
            {234, 98},
            {235, 99},
            {236, 99},
            {237, 99},
            {238, 99},
            {239, 99},
            {240, 99},
            {241, 99},
            {242, 99},
            {243, 99},
            {244, 99},
            {245, 99},
            {246, 99},
            {247, 99},
            {248, 100},
            {249, 100},
            {250, 100},
            {251, 100},
            {252, 100},
            {253, 100},
            {254, 100},
            {255, 100}
    };
}
